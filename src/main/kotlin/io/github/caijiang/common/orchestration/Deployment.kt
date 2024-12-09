package io.github.caijiang.common.orchestration

/**
 * 部署基本信息
 * @author CJ
 */
data class Deployment(
    val imageUrl: String,
    val imageTag: String,
    /**
     * 环境
     */
    val environment: Map<String, String>? = null,
)
