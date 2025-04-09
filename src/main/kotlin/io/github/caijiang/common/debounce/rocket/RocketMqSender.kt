package io.github.caijiang.common.debounce.rocket

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.debounce.DelayMQData
import io.github.caijiang.common.debounce.MqSender
import io.github.caijiang.common.debounce.config.DebounceProperties
import org.apache.rocketmq.spring.core.RocketMQTemplate
import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * @author CJ
 */
@Service
class RocketMqSender(
    private val template: RocketMQTemplate,
    private val properties: DebounceProperties,
) : MqSender {

    private val supportCustomTime: Boolean = isBrokerAbove5(template)

    private fun isBrokerAbove5(@Suppress("UNUSED_PARAMETER") template: RocketMQTemplate): Boolean {
        if (properties.rocketMqAbove5 != null) return properties.rocketMqAbove5!!
//        val producer = template.producer
//        producer.defaultMQProducerImpl
//            .mqClientFactory
//            .mqClientAPIImpl
//            .getBrokerClusterInfo()
        return false
    }


    private val rocketMqDelayLevels = mapOf(
        Duration.ofSeconds(1) to 1,
        Duration.ofSeconds(5) to 2,
        Duration.ofSeconds(10) to 3,
        Duration.ofSeconds(30) to 4,
        Duration.ofMinutes(1) to 5,
        Duration.ofMinutes(2) to 6,
        Duration.ofMinutes(3) to 7,
        Duration.ofMinutes(4) to 8,
        Duration.ofMinutes(5) to 9,
        Duration.ofMinutes(6) to 10,
        Duration.ofMinutes(7) to 11,
        Duration.ofMinutes(8) to 12,
        Duration.ofMinutes(9) to 13,
        Duration.ofMinutes(10) to 14,
        Duration.ofMinutes(20) to 15,
        Duration.ofMinutes(30) to 16,
        Duration.ofHours(1) to 17,
        Duration.ofHours(2) to 18
    )

    override fun sendDelay(message: Message<DelayMQData>, duration: Duration) {
        val result = if (!supportCustomTime) {
            log.trace("rocketMQ 5.0 -- template.syncSend")
            template.syncSend(
                properties.topic, message, template.producer.sendMsgTimeout.toLong(),
                rocketMqDelayLevels[duration]
                    ?: throw IllegalStateException("RocketMQ 5.0 之前的版本，只能使用:${rocketMqDelayLevels.keys}")
            )
        } else {
            // 5.0
            log.trace("rocketMQ 5.0 ++ template.syncSendDelayTimeMills")
            template.syncSendDelayTimeMills(properties.topic, message, duration.toMillis())
        }
        log.debug("rocketMQ sendResult = {}", result)
    }
}