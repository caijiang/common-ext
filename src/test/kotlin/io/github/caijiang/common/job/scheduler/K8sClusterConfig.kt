package io.github.caijiang.common.job.scheduler

import kotlinx.serialization.Serializable

/**
 * @author CJ
 */
@Serializable
data class K8sClusterConfig(
    val podName: String,
    val port: Int,
    val autoOAuthToken: String
)
