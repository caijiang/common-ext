@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package io.github.caijiang.common.notify

import io.github.caijiang.common.notify.test.NotifyApp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * @author CJ
 */
@SpringBootTest(
    classes = [NotifyApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
class NotifyAutoConfigurationTest {
    @Test
    internal fun 非web不会引入(@Autowired(required = false) controller: DebugController?) {
        assertThat(controller)
            .isNull()
    }

    @Test
    internal fun 肯定会载入(@Autowired(required = false) service: SendNoticeService?) {
        assertThat(service)
            .`as`("肯定会载入")
            .isNotNull()
    }
}