package io.github.caijiang.common.aliyun.oss

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * @author CJ
 */
@ConfigurationProperties(prefix = "oss")
data class OssProperties(
    /**
     * 用于后端交互的入口
     */
    var endPoint: String? = null,
    var accessKey: String? = null,
    var accessSecret: String? = null,
    /**
     * 公开资源的 bucket
     */
    var pubBucket: String? = null,
    /**
     * 保护资源的 bucket
     */
    var prvBucket: String? = null,
    /**
     * 区域不可缺少
     */
    var region: String? = null,
    /**
     * 公开资源的访问路径
     */
    var domain: String? = null,
    /**
     * 默认的自动签名的有效时间，默认一个小时
     */
    var expireDuration: Duration? = null,
)
