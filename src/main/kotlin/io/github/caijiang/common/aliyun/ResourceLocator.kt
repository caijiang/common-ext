package io.github.caijiang.common.aliyun

/**
 * 阿里云上的资源定位
 * @author CJ
 */
data class ResourceLocator(
    val accessKeyId: String,
    val accessKeySecret: String,
    /**
     * 工作区域
     */
    val region: String,
)
