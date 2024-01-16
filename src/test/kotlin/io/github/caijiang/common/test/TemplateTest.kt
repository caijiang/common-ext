package io.github.caijiang.common.test

import io.github.caijiang.common.test.mvc_demo.MvcDemoApp
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.exchange
import kotlin.test.Test

/**
 * @author CJ
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [MvcDemoApp::class])
@ActiveProfiles("test")
class TemplateTest : AbstractSpringTest() {
    @Test
    fun well() {
        val template = createTestTemplate()
        val r1 = template.exchange<String>(
            "/echoUrlEncoded",
            HttpMethod.POST,
            ClassPathResource("/echoUrlEncode.properties").createUrlEncodeHttpEntityFromProperties()
        )
        assertThat(r1.statusCode.is2xxSuccessful)
            .isTrue()
        assertThat(r1.body)
            .isEqualTo("foo+bar")


        val r2 = template.exchange<String>(
            "/echoUrlEncoded",
            HttpMethod.POST,
            ClassPathResource("/echoUrlEncode.properties").inputStream.createUrlEncodeHttpEntityFromProperties()
        )
        assertThat(r2.statusCode.is2xxSuccessful)
            .isTrue()
        assertThat(r2.body)
            .isEqualTo("foo+bar")

    }

}