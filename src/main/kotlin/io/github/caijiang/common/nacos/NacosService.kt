package io.github.caijiang.common.nacos

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.orchestration.IngressEntrance
import io.github.caijiang.common.orchestration.Service
import io.github.caijiang.common.orchestration.ServiceNode
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

    override fun suspendNode(serviceNode: ServiceNode) {
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

    override fun resumedNode(serviceNode: ServiceNode) {
        OpenApiHelper.changeInstance(locator, serviceName, serviceNode.ip, serviceNode.port, mapOf("enabled" to true))
    }

    override fun checkWorkStatus(node: ServiceNode): Boolean {
        val list = jsonNodeAsServiceNodes()
        if (log.isTraceEnabled) {
            log.trace("checkWorkStatus:{}:{}", node.ip, node.port)
            log.trace("fact list:{}", list?.map {
                mapOf(
                    "ip" to it.ip,
                    "port" to it.port,
                    "work" to it.work(),
                )
            })
        }
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