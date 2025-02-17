package io.github.caijiang.common.orchestration

import io.github.caijiang.common.Slf4j
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.logging.LoggingApi
import io.github.caijiang.common.logging.toLoggingApi
import io.github.caijiang.common.orchestration.exception.NodeRelatedIllegalStateException
import org.apache.commons.io.output.NullOutputStream
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.config.hosts.HostConfigEntry
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
import org.apache.sshd.client.session.ClientSession
import org.springframework.boot.logging.LogLevel
import java.nio.charset.StandardCharsets
import java.rmi.RemoteException
import java.security.KeyPair
import java.time.Duration
import java.util.concurrent.*


/**
 * 负责在阿里云上部署或者升级服务
 * @author CJ
 */
@Slf4j
class ServiceDeployer(
    /**
     * 用以登录 ssh 服务器的密钥，建议一个就好
     */
    private val keyPair: Set<KeyPair>,
    /**
     * https://en.wikibooks.org/wiki/OpenSSH/Cookbook/Proxies_and_Jump_Hosts
     */
    private val proxyJump: String? = null,
    /**
     * 连接到 ssh 后执行的一些初始化动作
     */
    private val sshPrepareWork: ((session: ClientSession, node: ServiceNode, service: Service) -> Unit)? = null,
    /**
     * 在暂停服务流量进入了，先行等待一段时间
     */
    private val sleepAfterSuspend: Duration? = null,
    /**
     * 可定制的 ssh 连接器，其默认值就是直接连接
     */
    private val clientSessionFetcher: (SshClient, ServiceNode) -> ClientSession = { sshClient, node ->
        sshClient
            .let {
                if (proxyJump != null) {
                    it.connect(HostConfigEntry("", node.ip, 22, "root", proxyJump))
                } else
                    it.connect("root", node.ip, 22)
            }
            .verify(10000)
            .session
    }

) {
    private val executorService = Executors.newCachedThreadPool()

    protected fun finalize() {
        executorService.shutdownNow()
    }

    /**
     * 1. 流量下线
     * 1. 检查流量
     * 1. 更新服务
     * 1. health check
     * 1. 流量上线
     * @see NodeRelatedThrowable
     */
    fun deploy(
        service: Service,
        entrances: Collection<IngressEntrance>,
        deployment: Deployment,
        logForService: LoggingApi = log.toLoggingApi(),
        logForNode: (
            node: ServiceNode,
            level: LogLevel,
            message: String,
            throwable: Throwable?
        ) -> Unit = { node, level, message, t ->
            logForService.logMessage(level, "${node.ip}:${node.port} " + message, t)
        },
        nodesAware: (List<ServiceNode>) -> Unit = { nodes ->
            log.info("即将部署到:{}", nodes.map { "${it.ip}:${it.port}" })
        },
        nodeStageChanger: (ServiceNode, NodeDeployStage) -> Unit = { node, stage ->
            log.info("${node.ip}:${node.port}" + "已经到了:{}", stage)
        },
        /**
         * 是否重新启动，在重新启动的时候 是无需检查当前是否已正常部署
         */
        restart: Boolean = false
    ) {
        // 寻找节点
        logForService.logMessage(LogLevel.INFO, "开始部署服务:${service.id}", null)

        val allNodes = entrances.associateWith { it.discoverNodes(service) }

        val nodes = allNodes.values.flatten()
            .distinctBy { it.ip to it.port }
        nodesAware(nodes)

        logForService.logMessage(LogLevel.INFO, "有${nodes.size} 个节点需要处理", null)

        val findMatchNode: (IngressEntrance, ServiceNode) -> ServiceNode? = { entrance, input ->
            allNodes[entrance]?.find { it.ip == input.ip && it.port == input.port }
        }

        for (node in nodes) {
            val logForThisNode = object : LoggingApi {
                override fun logMessage(level: LogLevel, message: String, throwable: Throwable?) {
                    logForNode(node, level, message, throwable)
                }
            }
            nodeStageChanger(node, NodeDeployStage.Prepare)
            logForThisNode.logMessage(LogLevel.INFO, "开始部署作业", null)

            SshClient.setUpDefaultClient()
                .use { sshClient ->
                    sshClient.serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
                    sshClient.start()
                    clientSessionFetcher(sshClient, node)
                        .use { session ->
                            keyPair.forEach { session.addPublicKeyIdentity(it) }
                            session.auth().verify(10000)
                            logForThisNode.logMessage(
                                LogLevel.DEBUG,
                                "成功登录 SSH,准备执行预制脚本以及自定义脚本",
                                null
                            )
                            sshPrepareWork?.let { it(session, node, service) }

                            logForThisNode.logMessage(LogLevel.INFO, "开始依赖环境检查，包括权限，基础设施等...", null)
                            environmentCheck(session, node, service, logForThisNode)

                            logForThisNode.logMessage(LogLevel.DEBUG, "检查是否需要部署", null)
                            if (!restart && targetAlreadyDeploy(session, deployment)) {
                                logForThisNode.logMessage(
                                    LogLevel.INFO,
                                    "node:${node.ip} already deployed (如果需要重新部署可通过 restart 指令)",
                                    null
                                )
                            } else {
                                val il = node.ingressLess
                                // 流量下线
                                nodeStageChanger(node, NodeDeployStage.SuspendIngress)
                                if (!il) {
                                    logForThisNode.logMessage(LogLevel.INFO, "停止流量进入……", null)
                                    entrances.forEach { entrance ->
                                        findMatchNode(entrance, node)?.let {
                                            runInNode(
                                                "停止${entrance.ingressName}流量进入",
                                                it,
                                                logForThisNode,
                                                {
                                                    while (true) {
                                                        try {
                                                            logForThisNode.logMessage(
                                                                LogLevel.TRACE,
                                                                "执行停止${entrance.ingressName}流量进入",
                                                                null
                                                            )
                                                            entrance.suspendNode(it, logForThisNode)
                                                            break
                                                        } catch (e: Exception) {
                                                            logForThisNode.logMessage(
                                                                LogLevel.TRACE,
                                                                "尝试停止流量时",
                                                                e
                                                            )
                                                            Thread.sleep(3000)
                                                        }
                                                    }
                                                },
                                                3,
                                                TimeUnit.MINUTES
                                            )
                                        }

                                    }
                                    nodeStageChanger(node, NodeDeployStage.CheckIngress)
                                    logForThisNode.logMessage(LogLevel.DEBUG, "检查流量是否已经走完", null)
                                    runInNode("检查流量是否走完", node, logForThisNode, {
                                        sleepAfterSuspend?.let {
                                            logForThisNode.logMessage(
                                                LogLevel.INFO,
                                                "检查具体流量前，先等上:${it}",
                                                null
                                            )
                                            Thread.sleep(it.toMillis())
                                        }
                                        while (true) {
                                            try {
                                                val cmd = "ss -an |grep \"${node.ip}:${node.port}\"|grep ESTAB"
                                                logForThisNode.logMessage(LogLevel.TRACE, "准备执行:$cmd", null)
                                                session.executeRemoteCommand(cmd)
                                                logForThisNode.logMessage(LogLevel.TRACE, "$cmd 成功执行", null)
                                                Thread.sleep(5000)
                                            } catch (e: RemoteException) {
                                                logForThisNode.logMessage(LogLevel.TRACE, "检查流量", e)
                                                break
                                            }
                                        }

                                    }, 4, TimeUnit.MINUTES)
                                }

                                nodeStageChanger(node, NodeDeployStage.Execute)
                                logForThisNode.logMessage(LogLevel.INFO, "执行部署指令...", null)

                                runInNode("执行部署", node, logForThisNode, {
                                    val withEnv = service.environment?.let { stringStringMap ->
                                        stringStringMap.entries
                                            .filter { it.key.isNotEmpty() && it.value.isNotEmpty() }
                                            .joinToString("") {
                                                " ${it.key} ${it.value}"
                                            }
                                    } ?: ""

                                    val cmd =
                                        "${service.deployCommand} ${service.id} ${node.ip} ${node.port} ${deployment.imageUrl} ${deployment.imageTag} ${service.type ?: "\"\""}$withEnv"
                                    logForThisNode.logMessage(LogLevel.TRACE, "准备执行:$cmd", null)
                                    session.executeRemoteCommand(
                                        cmd,
                                        NullOutputStream.NULL_OUTPUT_STREAM,
                                        StandardCharsets.US_ASCII
                                    )
                                }, 5, TimeUnit.MINUTES, true)

                                nodeStageChanger(node, NodeDeployStage.HealthCheck)
                                logForThisNode.logMessage(LogLevel.INFO, "执行健康检查...", null)

                                runInNode("健康检查", node, logForThisNode, {
                                    val hc = service.healthCheck
                                    val cmd = hc.toHealthCheckCommand(node)
                                    while (true) {
                                        logForThisNode.logMessage(LogLevel.TRACE, "准备执行:$cmd", null)
                                        try {
                                            val data = session.executeRemoteCommand(cmd)
                                            if (hc.checkHealth(data, 0)) {
                                                break
                                            } else {
                                                Thread.sleep(3000)
                                            }
                                        } catch (e: RemoteException) {
                                            logForThisNode.logMessage(LogLevel.TRACE, "健康检查错误:${e.message}", null)
                                            if (hc.checkHealth(null, 1)) {
                                                break
                                            } else {
                                                Thread.sleep(3000)
                                            }
                                        }
                                    }
                                }, 6, TimeUnit.MINUTES)

                                if (!il) {
                                    nodeStageChanger(node, NodeDeployStage.ResumeIngress)
                                    logForThisNode.logMessage(LogLevel.INFO, "恢复流量进入...", null)
                                    entrances.forEach { entrance ->
                                        findMatchNode(entrance, node)?.let {
                                            runInNode(
                                                "恢复${entrance.ingressName}流量进入",
                                                it,
                                                logForThisNode,
                                                {
                                                    while (true) {
                                                        try {
                                                            logForThisNode.logMessage(
                                                                LogLevel.TRACE,
                                                                "执行恢复${entrance.ingressName}流量进入",
                                                                null
                                                            )
                                                            entrance.resumedNode(it, logForThisNode)
                                                            break
                                                        } catch (e: Exception) {
                                                            logForThisNode.logMessage(
                                                                LogLevel.TRACE,
                                                                "尝试恢复流量时",
                                                                e
                                                            )
                                                            Thread.sleep(3000)
                                                        }
                                                    }

                                                },
                                                3,
                                                TimeUnit.MINUTES
                                            )
                                        }

                                    }

                                    nodeStageChanger(node, NodeDeployStage.Post)
                                    logForThisNode.logMessage(LogLevel.INFO, "检查流量是否进入...", null)
                                    entrances.forEach { entrance ->
                                        findMatchNode(entrance, node)?.let {
                                            runInNode(
                                                "检查${entrance.ingressName}流量是否可以正常进入了",
                                                it,
                                                logForThisNode,
                                                {
                                                    while (true) {
                                                        logForThisNode.logMessage(
                                                            LogLevel.TRACE,
                                                            "执行检查${entrance.ingressName}流量是否可以正常进入",
                                                            null
                                                        )
                                                        if (entrance.checkWorkStatus(it, logForThisNode))
                                                            break
                                                        Thread.sleep(3000)
                                                    }
                                                },
                                                6,
                                                TimeUnit.MINUTES
                                            )
                                        }

                                    }
                                }

                            }

                        }
                }
            nodeStageChanger(node, NodeDeployStage.Done)
        }

        logForService.logMessage(LogLevel.INFO, "${service.id}已完成部署", null)
    }

    private fun environmentCheck(
        session: ClientSession,
        node: ServiceNode,
        service: Service,
        loggingApi: LoggingApi,
    ) {
        runInNode("依赖环境检查", node, loggingApi, {
            try {
                val docker = session.executeRemoteCommand("docker -v")
                loggingApi.logMessage(LogLevel.DEBUG, "docker:$docker", null)
            } catch (e: Exception) {
                throw NodeRelatedIllegalStateException(node, "缺少 docker,或者缺少执行的权限")
            }
            try {
                val curl = session.executeRemoteCommand("curl --version")
                loggingApi.logMessage(LogLevel.DEBUG, "curl:$curl", null)
            } catch (e: Exception) {
                throw NodeRelatedIllegalStateException(node, "缺少 curl")
            }
            try {
                val ss = session.executeRemoteCommand("ss -v")
                loggingApi.logMessage(LogLevel.DEBUG, "ss $ss", null)
            } catch (e: Exception) {
                throw NodeRelatedIllegalStateException(node, "缺少 ss")
            }
            try {
                val dc = session.executeRemoteCommand("command -v ${service.deployCommand}")
                loggingApi.logMessage(LogLevel.DEBUG, "command $dc", null)
            } catch (e: Exception) {
                throw NodeRelatedIllegalStateException(node, "缺少可执行的${service.deployCommand}")
            }
        }, 10)
    }

    /**
     * 是否已部署
     */
    private fun targetAlreadyDeploy(session: ClientSession, deployment: Deployment): Boolean {
        try {
            session.executeRemoteCommand("docker ps|grep ${deployment.imageUrl}:${deployment.imageTag}")
            return true
        } catch (e: RemoteException) {
            return false
        }
    }

    private fun <T> runInNode(
        taskName: String,
        node: ServiceNode,
        loggingApi: LoggingApi,
        func: () -> T?,
        timeout: Long,
        unit: TimeUnit = TimeUnit.SECONDS,
        allowTimeout: Boolean = false
    ): T? {
        val future = executorService.submit(Callable { func() })
        try {
            return future.get(timeout, unit)
        } catch (e: ExecutionException) {
//            throw e.cause!!
            throw NodeRelatedIllegalStateException(node, e.message, e.cause)
        } catch (e: TimeoutException) {
            if (allowTimeout) {
                loggingApi.logMessage(LogLevel.INFO, "执行${taskName}时超时,但有可能是正常情况", null)
                return null
            }
            future.cancel(true)
            throw NodeRelatedIllegalStateException(node, "执行${taskName}时超时", e)
        }
    }

}