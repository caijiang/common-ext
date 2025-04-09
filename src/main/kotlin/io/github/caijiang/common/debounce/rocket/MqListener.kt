package io.github.caijiang.common.debounce.rocket

import io.github.caijiang.common.debounce.DelayMQData
import io.github.caijiang.common.debounce.bean.MqMessageHandler
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener
import org.apache.rocketmq.spring.core.RocketMQListener
import org.springframework.stereotype.Component

/**
 * @author CJ
 */
@Component
@RocketMQMessageListener(
    topic = "\${common.debounce.topic}", consumerGroup = "\${common.debounce.rocketMqConsumerGroup}"
)
class MqListener(
    private val mqMessageHandler: MqMessageHandler
) : RocketMQListener<DelayMQData> {

    override fun onMessage(message: DelayMQData) {
        // 业务直接写这里先，反正也只提供了 rocketmq 一个实现
        mqMessageHandler.handleIt(message)


    }
}