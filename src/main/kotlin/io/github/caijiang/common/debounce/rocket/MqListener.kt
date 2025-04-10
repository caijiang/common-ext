package io.github.caijiang.common.debounce.rocket

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.debounce.bean.MqMessageHandler
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener
import org.apache.rocketmq.spring.core.RocketMQListener
import org.springframework.stereotype.Component

/**
 * @author CJ
 */
@Component
@RocketMQMessageListener(
    topic = "\${common.debounce.topic}", consumerGroup = "\${common.debounce.rocket-mq-consumer-group:}"
)
class MqListener(
    private val mqMessageHandler: MqMessageHandler
) : RocketMQListener<String> {

    override fun onMessage(message: String) {
        log.debug("MqListener onMessage: {}", message)
        // 业务直接写这里先，反正也只提供了 rocketmq 一个实现
        mqMessageHandler.handleIt(RocketMqSender.rocketMqMapper.readValue(message))


    }
}