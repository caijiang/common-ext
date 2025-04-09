package io.github.caijiang.common.debounce

import org.springframework.messaging.Message
import java.time.Duration

/**
 * 内部使用的 mq 发送者
 * @author CJ
 */
interface MqSender {
    /**
     * 发送延时消息
     * @param duration 间隔
     */
    fun sendDelay(message: Message<DelayMQData>, duration: Duration)
}