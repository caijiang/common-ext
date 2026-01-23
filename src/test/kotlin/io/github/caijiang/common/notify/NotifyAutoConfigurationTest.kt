@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package io.github.caijiang.common.notify

import io.github.caijiang.common.debounce.test.DebounceApp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * @author CJ
 */
@SpringBootTest(
    classes = [DebounceApp::class],
)
class NotifyAutoConfigurationTest {
    @Test
    internal fun 肯定会载入(@Autowired(required = false) service: SendNoticeService?) {
        assertThat(service)
            .`as`("肯定会载入")
            .isNotNull()

        service?.send(object : Notifiable {
            override val urgentRole: UrgentRole
                get() = UrgentRole.Technology
            override val message: NotifiableMessage
                get() = object : NotifiableMessage {
                    override val title: String?
                        get() = null
                    override val textContent: String?
                        get() = null
                    override val specialReminder: SpecialNotifyTarget?
                        get() = null
                }
        })
    }
}