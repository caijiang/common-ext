package io.github.caijiang.common.wechat.data

/**
 * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#4
 * @author CJ
 */
data class JavascriptSignature(
    val appId: String,
    val timestamp: Long,
    val nonceStr: String,
    val signature: String
) : java.io.Serializable {
    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 2956232223336061L
    }
}

