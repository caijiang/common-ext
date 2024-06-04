package io.github.caijiang.common.wechat

import io.github.caijiang.common.wechat.support.SingleJvmCacheService
import org.assertj.core.api.Assertions.assertThat
import org.springframework.util.StringUtils
import kotlin.test.Test


/**
 * @author CJ
 */
class WechatHelperTest {

    private fun fetchAccount(): AppIdAndSecret? {
        val id = System.getenv("TEST_WECHAT_APP_ID")
        val secret = System.getenv("TEST_WECHAT_APP_SECRET")
        if (!StringUtils.hasLength(id) || !StringUtils.hasLength(secret)) {
            println("has no values to run")
            return null
        }
        return object : AppIdAndSecret {
            override val appId: String
                get() = id
            override val secret: String
                get() = secret
        }
    }

    @Test
    fun wechat() {
        val account = fetchAccount() ?: return

        val helper1 = WechatHelper(null)
        val at1 = helper1
            .queryStableAccessToken(account)

        assertThat(at1)
            .isNotNull

        val rs1 = helper1.javascriptSignature(account, "http://localhost")
        assertThat(rs1)
            .isNotNull
        println(rs1)

        val helper2 = WechatHelper(SingleJvmCacheService())

        val at2 = helper2.queryStableAccessToken(account)

        assertThat(helper2.queryStableAccessToken(account))
            .isEqualTo(at2)

    }

    @Test
    fun thisPage() {
        val account = fetchAccount() ?: return
        val helper2 = WechatHelper(SingleJvmCacheService())

        println(helper2.javascriptSignature(account, "http://0.0.0.0:9901/"))

    }


}