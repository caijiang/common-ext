package io.github.caijiang.common.nacos

import com.alibaba.nacos.api.PropertyKeyConst
import com.alibaba.nacos.api.naming.NamingFactory
import com.alibaba.nacos.api.naming.NamingMaintainFactory
import com.alibaba.nacos.api.naming.pojo.Instance
import io.github.caijiang.common.logging.LoggingApi
import io.github.caijiang.common.orchestration.IngressEntrance
import io.github.caijiang.common.orchestration.Service
import io.github.caijiang.common.orchestration.ServiceNode
import org.springframework.boot.logging.LogLevel
import java.util.*

/**
 * nacos 的一个服务
 * @author CJ
 */
class NacosService(
    private val serviceName: String,
    private val locator: ResourceLocator,
) : IngressEntrance {
    private val properties = Properties().apply {
        this[PropertyKeyConst.SERVER_ADDR] = locator.serverAddr
        locator.namespaceId?.let { this[PropertyKeyConst.NAMESPACE] = it }
        locator.auth?.let {
            this[PropertyKeyConst.USERNAME] = it.username
            this[PropertyKeyConst.PASSWORD] = it.password
        }
        if (locator.accessKey != null && locator.secretKey != null) {
            this[PropertyKeyConst.ACCESS_KEY] = locator.accessKey
            this[PropertyKeyConst.SECRET_KEY] = locator.secretKey
        }

        locator.clusterName?.let {
            this[PropertyKeyConst.ENDPOINT_CLUSTER_NAME] = it
        }

    }
    override val ingressName: String
        get() = "nacos"

    private fun changeInstance(
        serviceNode: ServiceNode,
        loggingApi: LoggingApi,
        optional: Boolean = true,
        block: Instance.() -> Unit
    ) {
        val allInstances = NamingFactory.createNamingService(properties).getAllInstances(serviceName)
        val target = allInstances.find {
            it.ip == serviceNode.ip && it.port == serviceNode.port
        } ?: allInstances.find { it.ip == serviceNode.ip }
        ?: if (optional) {
            loggingApi.logMessage(LogLevel.WARN, "没有在 nacos 找到符合:${serviceNode.ip} 的节点，按照策略跳过")
            return
        } else {
            loggingApi.logMessage(
                LogLevel.TRACE,
                "目标节点为${serviceNode.ip}，但我们找到的是:${allInstances.toMutableList()}"
            )
            loggingApi.logMessage(LogLevel.DEBUG, "虽然没有找到，根据之前的记录强行执行")
            NamingMaintainFactory.createMaintainService(properties)
                .updateInstance(serviceName, (serviceNode as NacosServiceNode).instance.apply(block))
            return
        }

        loggingApi.logMessage(LogLevel.DEBUG, "在nacos找到目标节点，执行业务操作")
        NamingMaintainFactory.createMaintainService(properties).updateInstance(serviceName, target.apply(block))
    }

    override fun suspendNode(serviceNode: ServiceNode, loggingApi: LoggingApi) {
//        loggingApi.logMessage(LogLevel.TRACE, "执行停止nacos流量进入")

        changeInstance(serviceNode, loggingApi) {
            this.isEnabled = false
        }
    }

    override fun resumedNode(serviceNode: ServiceNode, loggingApi: LoggingApi) {
//        loggingApi.logMessage(LogLevel.TRACE, "执行恢复nacos流量进入")

        changeInstance(serviceNode, loggingApi, false) {
            this.isEnabled = true
        }
    }

    override fun checkWorkStatus(node: ServiceNode, loggingApi: LoggingApi): Boolean {
        val list = NamingFactory.createNamingService(properties).getAllInstances(serviceName)
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


    override fun discoverNodes(service: Service): List<ServiceNode> {
        return NamingFactory.createNamingService(properties).getAllInstances(serviceName)
            .map {
                NacosServiceNode(it)
            }
    }

    class NacosServiceNode(
        val instance: Instance,
        override val ip: String = instance.ip,
        override val port: Int = instance.port,
        override val ingressLess: Boolean = !instance.isEnabled,
    ) : ServiceNode
}

private fun Instance.work(): Boolean {
    return this.isHealthy && this.isEnabled
}
