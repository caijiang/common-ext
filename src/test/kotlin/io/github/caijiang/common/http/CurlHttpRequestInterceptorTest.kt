package io.github.caijiang.common.http

import org.apache.hc.core5.http.io.HttpClientResponseHandler
import kotlin.test.Test

typealias HttpClientBuilder4 = org.apache.http.impl.client.HttpClientBuilder
typealias HttpClientBuilder5 = org.apache.hc.client5.http.impl.classic.HttpClientBuilder
typealias CookieStore4 = org.apache.http.impl.client.BasicCookieStore
typealias CookieStore5 = org.apache.hc.client5.http.cookie.BasicCookieStore
typealias HttpGet4 = org.apache.http.client.methods.HttpGet
typealias HttpPost4 = org.apache.http.client.methods.HttpPost
typealias HttpGet5 = org.apache.hc.client5.http.classic.methods.HttpGet
typealias HttpPost5 = org.apache.hc.client5.http.classic.methods.HttpPost
typealias EntityBuilder4 = org.apache.http.client.entity.EntityBuilder
typealias BasicNameValuePair4 = org.apache.http.message.BasicNameValuePair
typealias EntityBuilder5 = org.apache.hc.client5.http.entity.EntityBuilder
typealias BasicNameValuePair5 = org.apache.hc.core5.http.message.BasicNameValuePair
typealias ContentType4 = org.apache.http.entity.ContentType
typealias ContentType5 = org.apache.hc.core5.http.ContentType

/**
 * @author CJ
 */
class CurlHttpRequestInterceptorTest {
    private val stableUrlPrefix = "https://www.baidu.com"

    private val voidHandler = HttpClientResponseHandler<String> {
        null
    }

    @Test
    fun apache_http5() {
        HttpClientBuilder5.create()
            .addRequestInterceptorLast(CurlHttpRequestInterceptor5())
            .setDefaultCookieStore(CookieStore5())
            .build()
            .use { client ->
                val get1 = HttpGet5(stableUrlPrefix)
                client.execute(get1, voidHandler)

                val jsonPost = HttpPost5("$stableUrlPrefix/json")
                    .apply {
                        entity = EntityBuilder5.create()
                            .setContentType(ContentType5.APPLICATION_JSON)
                            .setText(""""{"string":"foo","array":[1,2,true]}""")
                            .build()
                    }

                client.execute(jsonPost, voidHandler)

                val formPost = HttpPost5("$stableUrlPrefix/form")
                    .apply {
                        entity = EntityBuilder5.create()
                            .setContentType(ContentType5.APPLICATION_FORM_URLENCODED)
                            .setParameters(
                                BasicNameValuePair5("foo", "v1"),
                                BasicNameValuePair5("bar", "v2"),
                            )
                            .build()
                    }

                client.execute(formPost, voidHandler)

            }
    }

    @Test
    fun apache_http4() {
        HttpClientBuilder4.create()
            .addInterceptorLast(CurlHttpRequestInterceptor4())
            .setDefaultCookieStore(CookieStore4())
            .build()
            .use { client ->
                val get1 = HttpGet4(stableUrlPrefix)
                client.execute(get1).close()

                val jsonPost = HttpPost4("$stableUrlPrefix/json")
                    .apply {
                        entity = EntityBuilder4.create()
                            .setContentType(ContentType4.APPLICATION_JSON)
                            .setText(""""{"string":"foo","array":[1,2,true]}""")
                            .build()
                    }

                client.execute(jsonPost).close()

                val formPost = HttpPost4("$stableUrlPrefix/form")
                    .apply {
                        entity = EntityBuilder4.create()
                            .setContentType(ContentType4.APPLICATION_FORM_URLENCODED)
                            .setParameters(
                                BasicNameValuePair4("foo", "v1"),
                                BasicNameValuePair4("bar", "v2"),
                            )
                            .build()
                    }

                client.execute(formPost).close()

            }
    }

}