package io.github.caijiang.common.debounce

import java.util.*

/**
 * @author CJ
 */
data class DelayMQData(
    /**
     * 业务类型
     */
    val type: String,
    /**
     * 业务参数
     */
    val arg: String,
    /**
     * 事务 id
     */
    val id: UUID,
    /**
     * 防抖目标时刻，跟存储系统必须保持完全一致！
     * @see System.currentTimeMillis
     */
    val debounceTimestamp: Long?
) {
    companion object {
        fun redisHashKeyFor(type: String, arg: String): String {
            return "debounce:${type}:${arg}"
        }
    }

    fun redisHashKey(): String {
        return redisHashKeyFor(type, arg)
    }
}
