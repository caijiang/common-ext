package io.github.caijiang.common.wechat

import io.github.caijiang.common.wechat.data.ExpiredToken

/**
 * 按照微信开发指引，各种 token 都有 cache 的必要
 * 如果应用只在单机运作可以考虑直接使用[io.github.caijiang.common.wechat.support.SingleJvmCacheService]实现
 * @author CJ
 */
interface TokenCacheService {
    /**
     * @return 获取缓存有效的 access Token, 其他情况可以 null
     */
    fun accessTokenCache(appId: String): ExpiredToken?

    /**
     * 更新新的 token
     */
    fun updateAccessToken(appId: String, token: ExpiredToken)

    /**
     * @return 获取缓存有效的 javascript api ticket, 其他情况可以 null
     */
    fun javascriptApiTicketCache(appId: String): ExpiredToken?

    /**
     * 更新新的 javascript api ticket
     */
    fun updateJavascriptApiTicket(appId: String, token: ExpiredToken)
}