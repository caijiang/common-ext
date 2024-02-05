package io.github.caijiang.common.test

import io.github.caijiang.common.test.jpa_rest_demo.JpaRestDemoApp
import org.junit.jupiter.api.condition.DisabledIf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.ClassUtils
import kotlin.test.Test

/**
 * @author CJ
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [JpaRestDemoApp::class])
@ActiveProfiles("test")
@DisabledIf(value = "check")
class JpaRestTest : AbstractSpringTest() {

    companion object {
        @JvmStatic
        fun check() = ClassUtils.isPresent("org.eclipse.persistence.Version", null)
    }


    @Test
    fun go() {
        val template = createTestTemplate()

//        println(template.getForObject<String>("/"))

        assertThat(
            template, "/departments", HttpMethod.POST, HttpEntity(
                mapOf(
                    "name" to "d1",
                    "enabled" to false,
                )
            )
        ).isSuccessResponse()

        assertThat(template, "/departments")
            .asSpringRestCollection()
            .print()
            .asEmbeddedList()
            .hasSize(1)
    }
}