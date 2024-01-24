package io.github.caijiang.common.test.solitary

import com.wix.mysql.Sources
import com.wix.mysql.distribution.Version
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.springframework.core.io.ClassPathResource


/**
 * @author CJ
 */
class SolitaryHelperTest {

    @Test
    fun redis() {
        SolitaryHelper.createRedis(null)
            .stop()

        Assertions.assertThat(System.getProperty("redis.port"))
            .isNotEmpty()
        Assertions.assertThat(System.getProperty("redis.password"))
            .isNotEmpty()

        SolitaryHelper.createRedis("abc")
            .stop()

        Assertions.assertThat(System.getProperty("redis.port"))
            .isNotEmpty()
        Assertions.assertThat(System.getProperty("redis.password"))
            .isNotEmpty()
            .isEqualTo("abc")
    }

    @Test
    @DisabledIfSystemProperty(named = "os.name", matches = "Linux")
    fun mysql() {
        SolitaryHelper.createMysql(null, null)
            .stop()

        SolitaryHelper.createMysql(Version.v5_7_latest, null)
            .stop()

        SolitaryHelper.createMysql(Version.v5_7_latest, { it -> it.withPort(5050) })
            .stop()

        SolitaryHelper.createMysql(
            Version.v5_7_latest, { it -> it.withPort(5050) },
            Sources.fromURL(ClassPathResource("/t1.sql").url)
        )
            .stop()

        Assertions.assertThat(System.getProperty("mysql.port"))
            .isNotEmpty()
        Assertions.assertThat(System.getProperty("mysql.database"))
            .isNotEmpty()
        Assertions.assertThat(System.getProperty("mysql.username"))
            .isNotEmpty()
        Assertions.assertThat(System.getProperty("mysql.password"))
            .isNotEmpty()
    }
}