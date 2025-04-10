package io.github.caijiang.common.debounce.rocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.debounce.DelayMQData
import io.github.caijiang.common.debounce.MqSender
import io.github.caijiang.common.debounce.config.DebounceProperties
import org.apache.rocketmq.spring.core.RocketMQTemplate
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
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
    companion object {
        val rocketMqMapper: ObjectMapper = ObjectMapper()
            .registerModules(
                KotlinModule.Builder()
                    .withReflectionCacheSize(512)
                    .configure(KotlinFeature.NullToEmptyCollection, false)
                    .configure(KotlinFeature.NullToEmptyMap, false)
                    .configure(KotlinFeature.NullIsSameAsDefault, false)
//                    .configure(KotlinFeature.SingletonSupport, DISABLED)
                    .configure(KotlinFeature.StrictNullChecks, true)
                    .build()
            )
    }

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
        val realMessage = MessageBuilder.withPayload(rocketMqMapper.writeValueAsString(message.payload))
            .copyHeaders(message.headers)
            .build()

        val result = if (!supportCustomTime) {
            log.trace("rocketMQ 5.0 -- template.syncSend with payload:{}", realMessage.payload)
            template.syncSend(
                properties.topic, realMessage, template.producer.sendMsgTimeout.toLong(),
                rocketMqDelayLevels[duration]
                    ?: throw IllegalStateException("RocketMQ 5.0 之前的版本，只能使用:${rocketMqDelayLevels.keys}")
            )
        } else {
            // 5.0
            log.trace("rocketMQ 5.0 ++ template.syncSendDelayTimeMills with payload:{}", realMessage.payload)
            template.syncSendDelayTimeMills(properties.topic, realMessage, duration.toMillis())
        }
        log.debug("rocketMQ sendResult = {}", result)
    }
}