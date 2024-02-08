package io.github.caijiang.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EXTKtTest {

    @Test
    fun removePrefixUntilLast() {
        assertThat("abc".removePrefixUntilLast("d"))
            .isEqualTo("abc")

        assertThat("abc".removePrefixUntilLast("b"))
            .isEqualTo("c")
        assertThat("abc".removePrefixUntilLast("c"))
            .isEqualTo("")
        assertThat("abc".removePrefixUntilLast("a"))
            .isEqualTo("bc")
    }
}