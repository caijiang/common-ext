package io.github.caijiang.common.util

import java.nio.ByteBuffer
import java.util.*

/**
 * @since 0.0.5
 * @author CJ
 */
object UtilUtils {
    @JvmStatic
    fun readBytesFrom(uuid: UUID): ByteArray {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)
        return bb.array()
    }

    @JvmStatic
    fun createUUIDFrom(bytes: ByteArray): UUID {
        var msb: Long = 0
        var lsb: Long = 0
        assert(bytes.size == 16) { "data must be 16 bytes in length" }
        for (i in 0..7) msb = (msb shl 8) or (bytes[i].toInt() and 0xff).toLong()
        for (i in 8..15) lsb = (lsb shl 8) or (bytes[i].toInt() and 0xff).toLong()
        return UUID(msb, lsb)
    }
}