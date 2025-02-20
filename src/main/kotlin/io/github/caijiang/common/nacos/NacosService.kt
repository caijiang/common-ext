package io.github.caijiang.common.nacos

import io.github.caijiang.common.logging.LoggingApi
import io.github.caijiang.common.orchestration.IngressEntrance
import io.github.caijiang.common.orchestration.Service
import io.github.caijiang.common.orchestration.ServiceNode
import org.springframework.boot.logging.LogLevel
import java.time.Duration

/**
 * nacos 的一个服务
 * @author CJ
 */
class NacosService(
    private val serviceName: String,
    private val locator: ResourceLocator,
    /**
     * 在挂起后，等待的时间
     */
    private val waitAfterSuspend: Duration? = null,
) : IngressEntrance {
    override val ingressName: String
        get() = "nacos"

    override fun suspendNode(serviceNode: ServiceNode, loggingApi: LoggingApi) {
        loggingApi.logMessage(LogLevel.TRACE, "执行停止nacos流量进入")
        OpenApiHelper.changeInstance(locator, serviceName, serviceNode.ip, serviceNode.port, mapOf("enabled" to false))
        waitAfterSuspend?.let {
            Thread.sleep(it.toMillis())
        }
    }

    fun suspendNodeByIP(ip: String) {
        val list = jsonNodeAsServiceNodes()
            ?: throw IllegalStateException("无法通过 listInstances 获取当前实例列表。停止流量:$ip 失败")
        val target = list.firstOrNull { it.ip == ip }
            ?: throw IllegalStateException("无法在 listInstances 结果中获取符合要求的实例。停止流量:$ip 失败")

        suspendNode(target)
    }

    override fun resumedNode(serviceNode: ServiceNode, loggingApi: LoggingApi) {
        loggingApi.logMessage(LogLevel.TRACE, "执行恢复nacos流量进入")
        OpenApiHelper.changeInstance(locator, serviceName, serviceNode.ip, serviceNode.port, mapOf("enabled" to true))
    }

    override fun checkWorkStatus(node: ServiceNode, loggingApi: LoggingApi): Boolean {
        val list = jsonNodeAsServiceNodes()
        loggingApi.logMessage(LogLevel.TRACE, "checkWorkStatus:${node.ip}:${node.port}")
        loggingApi.logMessage(
            LogLevel.TRACE, "fact list:${
                list?.map {
                    mapOf(
                        "ip" to it.ip,
                        "port" to it.port,
                        "work" to it.work(),
                    )
                }
            }"
        )
        return list?.filter {
            it.ip == node.ip
//            TODO 其实不太优雅的
//                    && it.port == node.port
        }?.any {
            it.work()
        } == true
    }

    private fun jsonNodeAsServiceNodes() =
        OpenApiHelper.listInstances(locator, serviceName)?.map { JsonNodeAsServiceNode(it) }

    override fun discoverNodes(service: Service): List<ServiceNode> {
        return listOf()
    }
}