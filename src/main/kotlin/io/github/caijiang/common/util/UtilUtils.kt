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

    /**
     * @see UUID
     */
    @JvmStatic
    fun createUUIDFrom(bytes: ByteArray): UUID {
        val x = ByteBuffer.wrap(bytes)
        return UUID(x.getLong(), x.getLong())
    }

    /**
     * junit,testing 等等
     * @return 当前是否运行在单元测试线程上
     * @since 2.3.0
     */
    @JvmStatic
    fun runInTest(): Boolean {
        val traces = Thread.currentThread().stackTrace
        for (element in traces) {
            if (element.className.startsWith("org.junit.") ||
                element.className.startsWith("org.testng.") ||
                element.className.contains("Test")
            ) {
                return true
            }
        }
        return false
    }
}