package io.github.caijiang.common.test

import com.fasterxml.jackson.databind.node.JsonNodeType
import io.github.caijiang.common.test.jpa_rest_demo.JpaRestDemoApp
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.condition.DisabledIf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.ClassUtils
import java.time.YearMonth
import kotlin.test.Test

/**
 * @author CJ
 */
@Suppress("TestFunctionName", "NonAsciiCharacters")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [JpaRestDemoApp::class])
@ActiveProfiles("test")
@DisabledIf(value = "check")
class JpaRestTest : AbstractSpringTest() {

    companion object {
        @JvmStatic
        fun check() = ClassUtils.isPresent("org.eclipse.persistence.Version", null)
    }

    @Test
    fun 复杂的组合的主键仓库() {
        val template = createTestTemplate()

        val d1Href = assertThat(
            template, "/departments", HttpMethod.POST, HttpEntity(
                mapOf(
                    "name" to RandomStringUtils.randomAlphabetic(20),
                    "enabled" to false,
                )
            )
        )
            .isSuccessResponse()
            .readFromEntity {
                it.headers["Location"]!!.first()
            }

        val ms1 = assertThat(
            template, "/departmentMonthScores", HttpMethod.POST, HttpEntity(
                mapOf(
                    "month" to YearMonth.now().toString(),
                    "pk" to mapOf(
                        "month" to YearMonth.now().toString(),
                    ),
                    "department" to d1Href,
                    "score" to 10
                )
            )
        )
            .print()
            .isSuccessResponse()
            .readFromEntity {
                it.headers["Location"]!!.first()
            }

        println(ms1)

        assertThat(template, "/departmentMonthScores")
            .print()
            .readFromData {
                it.body
            }
    }

    @Test
    fun go() {
        val template = createTestTemplate()

//        println(template.getForObject<String>("/"))
        val currentTotal = assertThat(template, "/departments")
            .asSpringRestCollection()
            .readTotal()!!

        println("currentTotal: $currentTotal")

        val department1Name = "d1"
        assertThat(
            template, "/departments", HttpMethod.POST, HttpEntity(
                mapOf(
                    "name" to department1Name,
                    "enabled" to false,
                )
            )
        )
            .print()
            .isSuccessResponse()

        val d1href = assertThat(template, "/departments")
            .asSpringRestCollection()
            .print()
            .total(currentTotal + 1)
            .asEmbeddedList()
            .hasSize((currentTotal + 1).toInt())
            .last()
            .print()
            .hasThisType(JsonNodeType.STRING, false, "name")
            .hasThisType(JsonNodeType.STRING, false) { it["name"] }
            .hasTextNode(department1Name, false, "name")
            .hasTextNode(department1Name, false) { it["name"] }
            .hasBooleanNode(expected = false, optional = false, path = "enabled")
            .hasBooleanNode(expected = false, optional = false) { it["enabled"] }
            .readSelfLink()

        assertThat(d1href)
            .isNotNull()

        assertThat(
            assertThat(template, "/int0")
                .readData<Int>()
        )
            .isEqualTo(0)
    }
}