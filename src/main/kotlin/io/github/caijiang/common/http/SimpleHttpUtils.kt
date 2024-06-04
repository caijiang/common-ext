package io.github.caijiang.common.http

import org.apache.hc.core5.http.ClassicHttpRequest
import org.apache.http.client.methods.HttpUriRequest
import org.springframework.util.ClassUtils

/**
 * 处理简单的 http 访问，这样就可以无视无视环境
 * @author CJ
 */
object SimpleHttpUtils {

    @JvmStatic
    fun httpAccess(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        requestBody: ByteArray? = null
    ): SimpleHttpResponse {
        return if (ClassUtils.isPresent("org.apache.hc.client5.http.impl.classic.HttpClientBuilder", null)) {
            org.apache.hc.client5.http.impl.classic.HttpClientBuilder.create()
                .setDefaultHeaders(
                    headers.entries.map { org.apache.hc.core5.http.message.BasicHeader(it.key, it.value) }
                )
                .build()
                .use { client ->
                    val request = createHttp5Request(url, method, requestBody)
                    client.execute(
                        request
                    ) { response ->
                        SimpleHttpResponse(
                            response!!.code,
                            response.headers
                                .distinctBy { it.name }
                                .associate { it.name to it.value },
                            response.entity?.let { org.apache.hc.core5.http.io.entity.EntityUtils.toByteArray(it) }
                        )
                    }
                }
        } else if (ClassUtils.isPresent("org.apache.http.impl.client.HttpClientBuilder", null)) {
            org.apache.http.impl.client.HttpClientBuilder.create()
                .setDefaultHeaders(
                    headers.entries.map {
                        org.apache.http.message.BasicHeader(it.key, it.value)
                    }
                )
                .build()
                .use { client ->
                    val request = createHttp4Request(url, method, requestBody)
                    client.execute(request)
                        .use { response ->
                            SimpleHttpResponse(
                                response.statusLine.statusCode,
                                response.allHeaders.distinctBy { it.name }
                                    .associate { it.name to it.value },
                                response.entity?.let {
                                    org.apache.http.util.EntityUtils.toByteArray(it)
                                }
                            )
                        }
                }
        } else {
            throw IllegalStateException("either apache http4 or apache http5 in classpath")
        }
    }

    private fun createHttp4Request(url: String, method: String, requestBody: ByteArray?): HttpUriRequest {
        val request = if (method.equals("GET", true)) {
            org.apache.http.client.methods.HttpGet(url)
        } else if (method.equals("trace", true)) {
            org.apache.http.client.methods.HttpTrace(url)
        } else if (method.equals("delete", true)) {
            org.apache.http.client.methods.HttpDelete(url)
        } else if (method.equals("head", true)) {
            org.apache.http.client.methods.HttpHead(url)
        } else if (method.equals("options", true)) {
            org.apache.http.client.methods.HttpOptions(url)
        } else if (method.equals("PUT", true)) {
            org.apache.http.client.methods.HttpPut(url)
        } else if (method.equals("POST", true)) {
            org.apache.http.client.methods.HttpPost(url)
        } else if (method.equals("PATCH", true)) {
            org.apache.http.client.methods.HttpPatch(url)
        } else {
            throw IllegalArgumentException("Unsupported method: $method")
        }

        requestBody?.let {
            (request as? org.apache.http.HttpEntityEnclosingRequest)?.entity =
                org.apache.http.client.entity.EntityBuilder.create()
                    .setBinary(it)
                    .build()
        }

        return request
    }

    private fun createHttp5Request(url: String, method: String, requestBody: ByteArray?): ClassicHttpRequest {
        val request = if (method.equals("GET", true)) {
            org.apache.hc.client5.http.classic.methods.HttpGet(url)
        } else if (method.equals("trace", true)) {
            org.apache.hc.client5.http.classic.methods.HttpTrace(url)
        } else if (method.equals("delete", true)) {
            org.apache.hc.client5.http.classic.methods.HttpDelete(url)
        } else if (method.equals("head", true)) {
            org.apache.hc.client5.http.classic.methods.HttpHead(url)
        } else if (method.equals("options", true)) {
            org.apache.hc.client5.http.classic.methods.HttpOptions(url)
        } else if (method.equals("PUT", true)) {
            org.apache.hc.client5.http.classic.methods.HttpPut(url)
        } else if (method.equals("POST", true)) {
            org.apache.hc.client5.http.classic.methods.HttpPost(url)
        } else if (method.equals("PATCH", true)) {
            org.apache.hc.client5.http.classic.methods.HttpPatch(url)
        } else {
            throw IllegalArgumentException("Unsupported method: $method")
        }

        requestBody?.let {
            request.entity = org.apache.hc.client5.http.entity.EntityBuilder.create()
                .setBinary(it)
                .build()
        }
        return request
    }
}