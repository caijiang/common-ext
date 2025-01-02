package io.github.caijiang.common.nacos

import io.github.caijiang.common.orchestration.IngressEntrance
import io.github.caijiang.common.orchestration.Service
import io.github.caijiang.common.orchestration.ServiceNode

/**
 * nacos 的一个服务
 * @author CJ
 */
class NacosService(
    private val serviceName: String,
    private val locator: ResourceLocator
) : IngressEntrance {

    override fun suspendNode(serviceNode: ServiceNode) {
        OpenApiHelper.changeInstance(locator, serviceName, serviceNode.ip, serviceNode.port, mapOf("enabled" to false))
    }

    override fun resumedNode(serviceNode: ServiceNode) {
        OpenApiHelper.changeInstance(locator, serviceName, serviceNode.ip, serviceNode.port, mapOf("enabled" to true))
    }

    override fun checkWorkStatus(node: ServiceNode): Boolean {
        return OpenApiHelper.listInstances(locator, serviceName)?.map { JsonNodeAsServiceNode(it) }?.filter {
            it.ip == node.ip && it.port == node.port
        }?.any {
            it.work()
        } == true
    }

    override fun discoverNodes(service: Service): List<ServiceNode> {
        return listOf()
    }
}