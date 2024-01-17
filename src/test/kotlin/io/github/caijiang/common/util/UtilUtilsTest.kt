package io.github.caijiang.common.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author CJ
 */
class UtilUtilsTest {

    @Test
    fun readBytesFrom() {
        val uuid = UUID.randomUUID()
        val buffer = uuid.bytes
        assertThat(UtilUtils.createUUIDFrom(buffer))
            .isEqualTo(uuid)
    }
}