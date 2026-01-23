package io.github.caijiang.common.notify.config

import io.github.caijiang.common.notify.NotifyChannel
import io.github.caijiang.common.notify.UrgentRole
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

/**
 * @author CJ
 */
@ConfigurationProperties(prefix = "common.notify")
data class NotifyConfiguration(
    // 配置通道,
    // 配置关注
    val roles: Map<UrgentRole, Array<TargetConfig>> = mapOf(),
    val threadSize: Int = 2
) {
    data class TargetConfig(
        /**
         * 渠道
         */
        val channel: NotifyChannel? = null,
        /**
         * 渠道配置
         */
        val properties: Properties? = null
    )
}
