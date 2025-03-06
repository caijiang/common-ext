package io.github.caijiang.common.servlet

import io.github.caijiang.common.test.AbstractSpringTest
import io.github.caijiang.common.test.mvc_demo.MvcDemoApp
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles

/**
 * @author CJ
 */
@Suppress("UastIncorrectHttpHeaderInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [MvcDemoApp::class])
@ActiveProfiles("test")
class RequestUtilsTest : AbstractSpringTest() {

    @Test
    fun go() {
        val template = createTestTemplate()

        assertThatRequest(
            template, "/log/abc", HttpMethod.POST, HttpEntity(null, HttpHeaders().apply {
                add("X-DEMO", "VALUE")
            })
        )
            .isSuccessResponse()
    }

}