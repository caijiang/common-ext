package io.github.caijiang.common.orchestration

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.logging.LoggingApi
import io.github.caijiang.common.logging.toLoggingApi

/**
 * 类似流量入口的概念
 * @author CJ
 */
interface IngressEntrance : NodeDiscoverer {
    /**
     * 流量名称
     */
    val ingressName: String

    /**
     * @param serviceNode 只可接受自己[NodeDiscoverer.discoverNodes]获得的值
     * 暂停将流量进入这个节点，这个动作很快就会完成
     */
    fun suspendNode(serviceNode: ServiceNode, loggingApi: LoggingApi = log.toLoggingApi())

    /**
     * @param serviceNode 只可接受自己[NodeDiscoverer.discoverNodes]获得的值
     * 恢复流量
     */
    fun resumedNode(serviceNode: ServiceNode, loggingApi: LoggingApi = log.toLoggingApi())

    /**
     * 检查工作状态
     * @param node 只可接受自己[NodeDiscoverer.discoverNodes]获得的值
     * @return 流量是否可以正常进入
     */
    fun checkWorkStatus(node: ServiceNode, loggingApi: LoggingApi = log.toLoggingApi()): Boolean
}