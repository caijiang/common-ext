package io.github.caijiang.common.test.solitary

import com.wix.mysql.Sources
import com.wix.mysql.distribution.Version
import io.github.caijiang.common.jdbc.mysqlCollateCorrect
import io.github.caijiang.common.mysql.MysqlCharsetCorrect
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import kotlin.test.Test


/**
 * @author CJ
 */
class SolitaryHelperTest {

    @Test
    fun env() {
        assertThat(SolitaryHelper.runInAliyunFlow {
            null
        }).isNull()

        assertThat(SolitaryHelper.runInAliyunFlow {
            if (it == "CI_RUNTIME_VERSION") "11"
            else null
        }).isNull()

        assertThat(SolitaryHelper.runInAliyunFlow {
            if (it == "CI_RUNTIME_VERSION") "11"
            else if (it == "caches") "[\"/root/.m2\",\"/root/.gradle/caches\",\"/root/.npm\",\"/root/.yarn\",\"/go/pkg/mod\",\"/root/.cache\"]"
            else null
        }).isNotNull
            .last()
            .isEqualTo("/root/.cache")

    }

    @Test
    fun redis() {
        SolitaryHelper.createRedis(null)
            .stop()

        assertThat(System.getProperty("redis.port"))
            .isNotEmpty()
        assertThat(System.getProperty("redis.password"))
            .isNotEmpty()

        SolitaryHelper.createRedis("abc")
            .stop()

        assertThat(System.getProperty("redis.port"))
            .isNotEmpty()
        assertThat(System.getProperty("redis.password"))
            .isNotEmpty()
            .isEqualTo("abc")
    }

    @Test
    @DisabledIfSystemProperty(named = "os.name", matches = "Linux")
    fun mysql() {
        SolitaryHelper.createMysql(null, null)
            .close()

        SolitaryHelper.createMysql(Version.v5_7_latest, null)
            .close()

        SolitaryHelper.createMysql(Version.v5_7_latest, { it.withPort(5050) })
            .close()

        SolitaryHelper.createMysql(
            Version.v5_7_latest, { it.withPort(5050) },
            Sources.fromURL(ClassPathResource("/t1.sql").url)
        )
            .apply {
                // 顺便测试 mysqlCollate
                JdbcTemplate(
                    DriverManagerDataSource(
                        "jdbc:mysql://${
                            System.getProperty(
                                "mysql.host",
                                "localhost"
                            )
                        }:5050/${System.getProperty("mysql.database")}",
                        System.getProperty("mysql.username"),
                        System.getProperty("mysql.password")
                    ).apply {
                        setDriverClassName("com.mysql.jdbc.Driver")
                    }
                ).mysqlCollateCorrect(
                    listOf(
                        MysqlCharsetCorrect.TextColumnCharset("t1", setOf("v1"), "utf8mb4")
                    )
                )
            }
            .close()

        assertThat(System.getProperty("mysql.port"))
            .isNotEmpty()
        assertThat(System.getProperty("mysql.database"))
            .isNotEmpty()
        assertThat(System.getProperty("mysql.username"))
            .isNotEmpty()
        assertThat(System.getProperty("mysql.password"))
            .isNotEmpty()
    }
}