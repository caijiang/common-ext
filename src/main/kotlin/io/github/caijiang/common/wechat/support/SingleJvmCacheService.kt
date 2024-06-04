package io.github.caijiang.common.wechat.support

import io.github.caijiang.common.wechat.TokenCacheService
import io.github.caijiang.common.wechat.data.ExpiredToken
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.Instant
import java.util.prefs.Preferences

/**
 * @author CJ
 */
class SingleJvmCacheService : TokenCacheService {
    private val store = Preferences.userNodeForPackage(SingleJvmCacheService::class.java)

    override fun accessTokenCache(appId: String): ExpiredToken? {
        return query("AccessToken", appId)
    }

    override fun updateAccessToken(appId: String, token: ExpiredToken) {
        updateNew("AccessToken", appId, token)
    }

    override fun javascriptApiTicketCache(appId: String): ExpiredToken? {
        return query("JAPI", appId)
    }

    override fun updateJavascriptApiTicket(appId: String, token: ExpiredToken) {
        updateNew("JAPI", appId, token)
    }

    private fun query(type: String, appId: String): ExpiredToken? {
        val str = store.getByteArray("x-$type-$appId", null) ?: return null
        return ObjectInputStream(ByteArrayInputStream(str))
            .use { stream ->
                (stream.readObject() as? ExpiredToken)?.let {
                    if (Instant.now().isBefore(it.expireTime)) {
                        it
                    } else
                        null
                }
            }
    }

    private fun updateNew(type: String, appId: String, token: ExpiredToken) {
        val buf = ByteArrayOutputStream()
        ObjectOutputStream(buf).use {
            it.writeObject(token)
            it.flush()
        }
        store.putByteArray("x-$type-$appId", buf.toByteArray())
    }
}