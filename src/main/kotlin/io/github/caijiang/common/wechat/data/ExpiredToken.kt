package io.github.caijiang.common.wechat.data

import java.time.Instant

/**
 * 名字稍微有点问题，应该是可过期的 token
 * @author CJ
 */
data class ExpiredToken(
    /**
     * 物理保存最少需要 512位
     */
    val token: String,
    /**
     * 过期的瞬间
     */
    val expireTime: Instant,
) : java.io.Serializable {
    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 2956232223339746061L
    }
}
