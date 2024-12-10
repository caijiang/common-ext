package io.github.caijiang.common.orchestration

import io.github.caijiang.common.Slf4j
import io.github.caijiang.common.Slf4j.Companion.log
import org.apache.commons.io.output.NullOutputStream
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
import org.apache.sshd.client.session.ClientSession
import java.nio.charset.StandardCharsets
import java.rmi.RemoteException
import java.security.KeyPair
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
     * 可定制的 ssh 连接器，其默认值就是直接连接
     */
    private val clientSessionFetcher: (SshClient, ServiceNode) -> ClientSession = { sshClient, node ->
        sshClient.connect("root", node.ip, 22)
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
     */
    fun deploy(
        service: Service,
        entrances: Collection<IngressEntrance>,
        deployment: Deployment,
        /**
         * 是否重新启动，在重新启动的时候 是无需检查当前是否已正常部署
         */
        restart: Boolean = false
    ) {
        // 寻找节点
        log.info("start deploy service:{}", service.id)

        val nodes = entrances.flatMap { it.discoverNodes(service) }
            .distinctBy { it.ip }

        log.info("有 ${nodes.size}  个节点需要处理")

        for (node in nodes) {
            log.info("开始处理:{}", node.ip)

            SshClient.setUpDefaultClient()
                .use { sshClient ->
                    sshClient.serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
                    sshClient.start()
                    clientSessionFetcher(sshClient, node)
                        .use { session ->
                            keyPair.forEach { session.addPublicKeyIdentity(it) }
                            session.auth().verify(10000)
                            log.debug("ssh login successful")

                            log.info("开始依赖环境检查，包括权限，基础设施等...")
                            environmentCheck(session, node, service)

                            log.debug("检查是否需要部署")
                            if (!restart && targetAlreadyDeploy(session, deployment)) {
                                log.info("node:{} already deployed (如果需要重新部署可通过 restart 指令)", node.ip)
                            } else {
                                val il = node.ingressLess

                                // 流量下线
                                if (!il) {
                                    log.info("停止流量进入{}...", node.ip)
                                    entrances.forEach {
                                        runIn("停止流量进入${node.ip}", {
                                            while (true) {
                                                try {
                                                    log.trace("执行停止流量进入{}", node.ip)
                                                    it.suspendNode(node)
                                                    break
                                                } catch (e: Exception) {
                                                    log.trace("尝试停止流量时", e)
                                                    Thread.sleep(3000)
                                                }
                                            }

                                        }, 3, TimeUnit.MINUTES)
                                    }
                                    log.debug("检查流量是否已经走完")
                                    runIn("检查流量是否走完", {
                                        while (true) {
                                            try {
                                                val cmd = "ss -an |grep \"${node.ip}:${node.port}\"|grep ESTAB"
                                                log.trace("preparing to execute:{}", cmd)
                                                session.executeRemoteCommand(cmd)
                                                log.trace("{}  可成功执行", cmd)
                                                Thread.sleep(5000)
                                            } catch (e: RemoteException) {
                                                log.trace("检查流量", e)
                                                break
                                            }
                                        }

                                    }, 3, TimeUnit.MINUTES)
                                }

                                log.info("执行部署指令...")
                                runIn("执行部署", {
                                    // TODO 环境暂不支持
                                    val cmd =
                                        "${service.deployCommand} ${service.id} ${node.ip} ${node.port} ${deployment.imageUrl} ${deployment.imageTag} ${service.type ?: "\"\""}"
                                    log.trace("preparing to execute:{}", cmd)
                                    session.executeRemoteCommand(
                                        cmd,
                                        NullOutputStream.NULL_OUTPUT_STREAM,
                                        StandardCharsets.US_ASCII
                                    )
                                }, 5, TimeUnit.MINUTES, true)

                                log.info("执行健康检查...")
                                runIn("健康检查", {
                                    val hc = service.healthCheck
                                    val cmd = hc.toHealthCheckCommand(node)
                                    while (true) {
                                        log.trace("preparing to execute:{}", cmd)
                                        try {
                                            val data = session.executeRemoteCommand(cmd)
                                            if (hc.checkHealth(data, 0)) {
                                                break
                                            } else {
                                                Thread.sleep(3000)
                                            }
                                        } catch (e: RemoteException) {
                                            log.trace("health check failed: {}", e.message)
                                            if (hc.checkHealth(null, 1)) {
                                                break
                                            } else {
                                                Thread.sleep(3000)
                                            }
                                        }
                                    }
                                }, 6, TimeUnit.MINUTES)

                                if (!il) {
                                    log.info("恢复流量进入{}...", node.ip)
                                    entrances.forEach {
                                        runIn("恢复流量进入${node.ip}", {
                                            while (true) {
                                                try {
                                                    log.trace("执行恢复流量进入{}", node.ip)
                                                    it.resumedNode(node)
                                                    break
                                                } catch (e: Exception) {
                                                    log.trace("尝试恢复流量时", e)
                                                    Thread.sleep(3000)
                                                }
                                            }

                                        }, 3, TimeUnit.MINUTES)
                                    }

                                    log.info("检查流量是否进入{}...", node.ip)
                                    entrances.forEach {
                                        runIn("检查流量是否可以正常进入${node.ip}了", {
                                            while (true) {
                                                log.trace("执行检查流量是否可以正常进入{}", node.ip)
                                                if (it.checkWorkStatus(node))
                                                    break
                                                Thread.sleep(3000)
                                            }

                                        }, 6, TimeUnit.MINUTES)
                                    }
                                }

                            }

                        }
                }
        }

        log.info("service:{} 已完成部署", service.id)

    }

    private fun environmentCheck(session: ClientSession, node: ServiceNode, service: Service) {
        runIn("依赖环境检查", {
            try {
                val docker = session.executeRemoteCommand("docker -v")
                log.debug("host:{}, docker: {}", node.ip, docker)
            } catch (e: Exception) {
                throw IllegalStateException("host:${node.ip} 缺少 docker,或者缺少执行的权限")
            }
            try {
                val curl = session.executeRemoteCommand("curl --version")
                log.debug("host:{}, curl: {}", node.ip, curl)
            } catch (e: Exception) {
                throw IllegalStateException("host:${node.ip} 缺少 curl")
            }
            try {
                val ss = session.executeRemoteCommand("ss -v")
                log.debug("host:{}, ss: {}", node.ip, ss)
            } catch (e: Exception) {
                throw IllegalStateException("host:${node.ip} 缺少 ss")
            }
            try {
                val dc = session.executeRemoteCommand("command -v ${service.deployCommand}")
                log.debug("host:{}, command: {}", node.ip, dc)
            } catch (e: Exception) {
                throw IllegalStateException("host:${node.ip} 缺少可执行的${service.deployCommand}")
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

    private fun <T> runIn(
        taskName: String,
        func: () -> T?,
        timeout: Long,
        unit: TimeUnit = TimeUnit.SECONDS,
        allowTimeout: Boolean = false
    ): T? {
        val future = executorService.submit(Callable { func() })
        try {
            return future.get(timeout, unit)
        } catch (e: ExecutionException) {
            throw e.cause!!
        } catch (e: TimeoutException) {
            if (allowTimeout) {
                log.info("执行{}时超时,但有可能是正常情况", taskName)
                return null
            }
            future.cancel(true)
            throw IllegalStateException("执行${taskName}时超时", e)
        }
    }
}