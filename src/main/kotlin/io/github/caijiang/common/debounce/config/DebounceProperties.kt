package io.github.caijiang.common.debounce.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author CJ
 */
@ConfigurationProperties(prefix = "common.debounce")
data class DebounceProperties(
    var enabled: Boolean = true,
    /**
     * 异步发送消息，默认是同步发送
     */
    var asyncSending: Boolean = true,
    /**
     * 延时 mq 的名称
     */
    var topic: String? = null,
    /**
     * 使用的是 5.0以上的 rocketMQ
     */
    var rocketMqAbove5: Boolean? = null,
    /**
     * 消费组
     */
    var rocketMqConsumerGroup: String? = null,
)
