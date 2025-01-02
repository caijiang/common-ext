package io.github.caijiang.common.wechat

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.caijiang.common.http.SimpleHttpResponse
import io.github.caijiang.common.http.SimpleHttpUtils
import io.github.caijiang.common.wechat.data.ExpiredToken
import io.github.caijiang.common.wechat.data.JavascriptSignature
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant

/**
 * @author CJ
 */
class WechatHelper(
    private val tokenCacheService: TokenCacheService?
) {
    private val log = LoggerFactory.getLogger(WechatHelper::class.java)
    private val objectMapper = ObjectMapper()

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/getStableAccessToken.html
     * @return accessToken
     */
    fun queryStableAccessToken(account: AppIdAndSecret): ExpiredToken {
        cacheServiceSafe()
        val token = tokenCacheService?.accessTokenCache(account.appId)
        if (token != null) {
            return token
        }

        val requestInstant = Instant.now()

        val response = SimpleHttpUtils.httpAccess(
            "https://api.weixin.qq.com/cgi-bin/stable_token",
            "POST",
            mapOf("content-type" to "application/json; encoding=utf-8")
        ) {
            it.setBinary(
                objectMapper.writeValueAsBytes(
                    mapOf(
                        "grant_type" to "client_credential",
                        "appid" to account.appId,
                        "secret" to account.secret,
                        "force_refresh" to false
                    )
                )
            )
        }

        val root = readWechatResponse(response)

        val newToken = ExpiredToken(
            root.get("access_token").textValue(), requestInstant.plusSeconds(root.get("expires_in").asLong())
        )

        tokenCacheService?.updateAccessToken(account.appId, newToken)

        return newToken
    }

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#62
     */
    private fun queryJavascriptApiTicket(account: AppIdAndSecret): ExpiredToken {
        cacheServiceSafe()

        val tokenn = tokenCacheService?.javascriptApiTicketCache(account.appId)

        if (tokenn != null) {
            return tokenn
        }

        val access = queryStableAccessToken(account)

        val requestInstant = Instant.now()
        val response =
            SimpleHttpUtils.httpAccess("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=${access.token}&type=jsapi")
        val root = readWechatResponse(response)

        val jt = ExpiredToken(
            root.get("ticket").textValue(), requestInstant.plusSeconds(root.get("expires_in").asLong())
        )
        tokenCacheService?.updateJavascriptApiTicket(account.appId, jt)
        return jt
    }

    /**
     * https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#62
     * @param url 当前网页的URL，不包含#及其后面部分
     */
    @Suppress("SpellCheckingInspection")
    fun javascriptSignature(account: AppIdAndSecret, url: String): JavascriptSignature {
        cacheServiceSafe()
        val jt = queryJavascriptApiTicket(account)
        val noncestr = RandomStringUtils.randomAlphabetic(12)
        val timestamp = System.currentTimeMillis() / 1000

        val str1 = listOf(
            "noncestr" to noncestr, "jsapi_ticket" to jt.token, "timestamp" to timestamp.toString(), "url" to url
        )
            .sortedBy { it.first }.joinToString("&") { "${it.first}=${it.second}" }

        val result = MessageDigest.getInstance("SHA1")
            .digest(str1.toByteArray(StandardCharsets.UTF_8))

        log.debug("ticket:{}", jt.token)
        log.debug("str1:{}", str1)

        return JavascriptSignature(
            account.appId, timestamp, noncestr, Hex.encodeHexString(result, true)
        )
    }

    private fun cacheServiceSafe() {
        if (tokenCacheService == null) {
            log.warn("按照微信开发指引，开发者有义务缓存获取到的 token.")
        }
    }

    private fun readWechatResponse(response: SimpleHttpResponse): JsonNode {
        if (response.status != 200) {
            throw IllegalStateException("wechat response ${response.status}")
        }
        val root = objectMapper.readTree(response.body)
        // errcode
        val errcode = root.get("errcode")
        if (errcode != null && !errcode.isNull) {
            if (errcode.isNumber && errcode.intValue() == 0) {
                return root
            }
            throw IllegalStateException("wechat response $root")
        }
        return root
    }

}