package io.github.caijiang.common.wechat

/**
 * 可以获取微信公众号 appId和 secret 的
 * @author CJ
 */
interface AppIdAndSecret {
    val appId: String
    val secret: String
}