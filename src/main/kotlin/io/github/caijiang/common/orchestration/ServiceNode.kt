package io.github.caijiang.common.orchestration

/**
 * 后端服务节点
 * @author CJ
 */
interface ServiceNode {
    val ip: String
    val port: Int

    /**
     * 是否处在没有流量进入的状态
     */
    val ingressLess: Boolean
}