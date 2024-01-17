package io.github.caijiang.common.util

import java.util.*

val UUID.bytes: ByteArray
    get() {
        return UtilUtils.readBytesFrom(this)
    }