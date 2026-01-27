@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package io.github.caijiang.common.notify

import io.github.caijiang.common.notify.test.NotifyApp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.boot.test.web.client.TestRestTemplate

/**
 * @author CJ
 */
@SpringBootTest(
    classes = [NotifyApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class NotifyAutoConfigurationWithWebTest {
    @SpyBean
//    @MockkBean
    lateinit var sendNoticeService: SendNoticeService

    @Test
    internal fun web必须引入(@Autowired(required = false) controller: DebugController?) {
        assertThat(controller)
            .isNotNull
    }

    @Test
    internal fun 试试调试(@Autowired template: TestRestTemplate) {
        template.getForEntity("/notify/debug", Nothing::class.java)
        Mockito.verify(sendNoticeService, times(UrgentRole.entries.size))
            .send(any())
    }
}