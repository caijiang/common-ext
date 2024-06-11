package io.github.caijiang.common.test

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.PayloadApplicationEvent
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.Test

/**
 * @author CJ
 */
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class EventTest : AbstractSpringTest() {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun go() {
        // 延迟 1s 发送时间
        Thread({
            Thread.sleep(1000)
            applicationContext.publishEvent("pleaseWaitUntilEvent")
        }, "event-firer")
            .apply {
                isDaemon = true
            }
            .start()

        pleaseWaitUntilEvent {
            if (it is PayloadApplicationEvent<*> && it.payload == "pleaseWaitUntilEvent")
                true
            else {
                println(it)
                false
            }
        }

        Thread({
            Thread.sleep(1000)
            applicationContext.publishEvent("pleaseWaitUntilPayloadApplicationEvent")
        }, "event-firer")
            .apply {
                isDaemon = true
            }
            .start()

        pleaseWaitUntilPayloadApplicationEvent {
            it == "pleaseWaitUntilPayloadApplicationEvent"
        }
    }

}