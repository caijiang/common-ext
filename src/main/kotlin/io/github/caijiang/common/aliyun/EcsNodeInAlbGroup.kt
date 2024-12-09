package io.github.caijiang.common.aliyun

import io.github.caijiang.common.orchestration.ServiceNode

/**
 * @author CJ
 */
data class EcsNodeInAlbGroup(
    val serverId: String,
    val serverGroupId: String,
    val serverIp: String,
    override val port: Int,
    val description: String?,
    val weight: Int,
) : ServiceNode {
    override val ip: String
        get() = serverIp
    override val ingressLess: Boolean
        get() = weight <= 0
}
