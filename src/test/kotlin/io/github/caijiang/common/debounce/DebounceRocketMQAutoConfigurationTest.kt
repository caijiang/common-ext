@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package io.github.caijiang.common.debounce

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.debounce.bean.MqMessageHandler
import io.github.caijiang.common.debounce.test.DebounceApp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.messaging.Message
import org.springframework.test.context.ActiveProfiles
import java.io.Serializable
import java.time.Duration

/**
 * 没有特别声明，我们不会载入
 * @author CJ
 */
@SpringBootTest(
    classes = [DebounceApp::class],
)
@ActiveProfiles("test")
class DebounceRocketMQAutoConfigurationTest {
    @Test
    internal fun 逃过自动载入(@Autowired(required = false) config: DebounceConfiguration?) {
        assertThat(config)
            .`as`("没有特别声明，我们不会载入")
            .isNull()
    }
}

/**
 * 优先使用 @TestConfiguration - 它比 @MockBean 更早注册，能更好地与条件注解配合
 */
@SpringBootTest(
    classes = [DebounceApp::class],
    properties = ["common.debounce.topic=abc", "common.debounce.rocketMqConsumerGroup=g", "rocketmq.name-server=X"]
)
@ActiveProfiles("test", "skipRocketMq")
class DebounceRocketMQAutoConfigurationTest2 {

    @Autowired
    private lateinit var app: DebounceApp

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    data class MyId(
        val id1: Int,
        val id2: Int
    ) : Serializable

    @Test
    internal fun 正常执行防抖业务(
        @Autowired(required = false) config: DebounceConfiguration?,
        @Autowired(required = false) debounceService: DebounceService?
    ) {
        assertThat(config)
            .`as`("履约行事则可载入")
            .isNotNull

        assertThat(debounceService)
            .isNotNull

        log.info("这里断言发出的消息")
        val testType = "ba"
        val testArg = MyId(1, 2)
        val testDebounceDuration = Duration.ofSeconds(5)
        val testDeathDuration = Duration.ofSeconds(10)

        val matchPair: (Pair<Message<DelayMQData>, Duration>) -> Boolean = {
            val msg = it.first.payload
            msg.type == testType && msg.arg == testArg
        }

        val matcherPairForType1: (Pair<Message<DelayMQData>, Duration>) -> Boolean = {
            val msg = it.first.payload
            matchPair(it) && msg.debounceTimestamp == null && it.second == testDeathDuration
        }
        val matcherPairForType2: (Pair<Message<DelayMQData>, Duration>) -> Boolean = {
            val msg = it.first.payload
            matchPair(it) && msg.debounceTimestamp != null && it.second == testDebounceDuration
        }

        debounceService?.debounce(
            testType, testArg, testDebounceDuration, testDeathDuration
        )
        assertThat(app.sendingMqMessages)
            .`as`("一共 2 个，一个 death 一个 防抖; 两个 id是一致的")
            .hasSize(2)
            .anyMatch(matcherPairForType1)
            .anyMatch(matcherPairForType2)
        val tid = app.sendingMqMessages.map { it.first.payload.id }.distinct()
            .apply {
                assertThat(this).hasSize(1)
            }
            .first()

        // 一个已过期的 mq
        val expiredDebounceMq = app.sendingMqMessages
            .first(matcherPairForType2)
        app.sendingMqMessages.clear()
        Thread.sleep(1L)
        debounceService?.debounce(
            testType, testArg, testDebounceDuration, testDeathDuration
        )
        assertThat(app.sendingMqMessages)
            .`as`("只有一个，就是防抖的")
            .hasSize(1)
            .anyMatch(matcherPairForType2)
            .anyMatch { it.first.payload.id == tid }

        log.info("发送 MQ 测试完成")

        val listener = applicationContext.getBean(MqMessageHandler::class.java)
        // 一个已过期的防抖消息; 不会产生任何反应
        listener.handleIt(expiredDebounceMq.first.payload)
        assertThat(app.invokingBusinessList)
            .isEmpty()
        // 一个未过期的防抖消息; 执行业务
        listener.handleIt(app.sendingMqMessages.first().first.payload)
        assertThat(app.invokingBusinessList)
            .hasSize(1)
            .containsExactly(testType to testArg)
        app.invokingBusinessList.clear()
        // 一个执行过的消息；不会产生任何反应
        listener.handleIt(app.sendingMqMessages.first().first.payload)
        assertThat(app.invokingBusinessList)
            .isEmpty()
    }
}

