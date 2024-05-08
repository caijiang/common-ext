package io.github.caijiang.common.test

import io.github.caijiang.common.test.mvc_demo.MvcDemoApp
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.exchange
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject
import kotlin.test.Test

/**
 * @author CJ
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [MvcDemoApp::class])
@ActiveProfiles("test")
class TemplateTest : AbstractSpringTest() {

    @Test
    fun changeToOtherType() {
        val template = createTestTemplate()

        assertThatRequest(template, "/readString")
            .isSuccessResponse()
            .asData(String::class.java)
            .isEqualTo("String")

        assertThat(listOf(""))
            .last()

        assertThatRequest(template, "/readList")
            .isSuccessResponse()
            .isListResponse()
            .asListAssert()
            .first()
            .hasTextNode("value1", false, "key1")

        assertThatRequest(template, "/readObject")
            .isSuccessResponse()
            .isObjectResponse()
            .asObject()
            .hasTextNode("value1", false, "key1")

        assertThatRequest(template, "/readComplexList")
            .asListAssert()
            .first()
            .assertData("key2")
            .hasTextNode("value3", false, "key3")

    }

    @Test
    fun assertion() {
        val template = createTestTemplate()

        assertThatRequest(template, "/echoUrlEncoded?p1=1&p2=2")
            .isLegalResponse()
            .isSuccessResponse()

        assertThatResponse(template.getForEntity("/echoUrlEncoded?p1=1&p2=2"))
            .isLegalResponse()

        assertThatRequest(template, "/echoUrlEncoded22?p1=1&p2=2")
            .isFailedResponse()
            .isErrorCodeMatch("404")

    }

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

        assertThat(template.getForObject<String?>("/readCookie"))
            .isNull()

        template.getForObject<String>("/setCookie")
        assertThat(template.getForObject<String>("/readCookie"))
            .isEqualTo("foo")
    }

}