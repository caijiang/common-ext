package io.github.caijiang.common.orchestration

/**
 * 类似流量入口的概念
 * @author CJ
 */
interface IngressEntrance : NodeDiscoverer {
    /**
     * 暂停将流量进入这个节点
     */
    fun suspendNode(serviceNode: ServiceNode)

    /**
     * 恢复流量
     */
    fun resumedNode(serviceNode: ServiceNode)

    /**
     * 检查工作状态
     */
    fun checkWorkStatus(node: ServiceNode): Boolean
}