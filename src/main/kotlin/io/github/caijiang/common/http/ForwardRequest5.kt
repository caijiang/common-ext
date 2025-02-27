package io.github.caijiang.common.http

import io.github.caijiang.common.HttpServletRequest
import io.github.caijiang.common.HttpServletResponse
import org.apache.hc.client5.http.classic.methods.*
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.http.ClassicHttpRequest
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.InputStreamEntity


/**
 * @since 2.2.0
 * @author CJ
 */
@Suppress("unused")
object ForwardRequest5 {

    private fun toHttpEntity(request: HttpServletRequest): HttpEntity? {
        if (request.contentLengthLong == 0L) return null
        return InputStreamEntity(request.inputStream, request.contentLengthLong, null)
    }

    private fun convert(request: HttpServletRequest, changer: NewHttpRequestUrl): ClassicHttpRequest {
        val method = request.method
        val url = changer.newUrl(request)
        val httpUriRequest: ClassicHttpRequest

        @Suppress("DuplicatedCode")
        when (method) {
            "GET" -> httpUriRequest = HttpGet(url)
            "POST" -> {
                val t = HttpPost(url)
                t.entity = toHttpEntity(request)
                httpUriRequest = t
            }

            "PUT" -> {
                val t = HttpPut(url)
                t.entity = toHttpEntity(request)
                httpUriRequest = t
            }

            "PATCH" -> {
                val t = HttpPatch(url)
                t.entity = toHttpEntity(request)
                httpUriRequest = t
            }

            "DELETE" -> httpUriRequest = HttpDelete(url)
            else -> throw UnsupportedOperationException("Unsupported HTTP method: $method")
        }

        // 复制请求头
        val headerNames = request.headerNames
        while (headerNames.hasMoreElements()) {
            val headerName: String = headerNames.nextElement()
            if (ForwardRequest.ignoreHeaders(headerName)) {
                continue
            }
            val headerValue = request.getHeader(headerName)
            httpUriRequest.setHeader(headerName, headerValue)
        }

        return httpUriRequest
    }

    @JvmStatic
    fun forward(request: HttpServletRequest, response: HttpServletResponse, changer: NewHttpRequestUrl) {
        // 转发请求
        HttpClientBuilder.create().build().use { client ->
            client.execute(
                convert(
                    request, changer
                ), { httpResponse ->
                    try {
                        response.status = httpResponse.code
                        httpResponse.headers
                            .filter { !ForwardRequest.ignoreHeaders(it.name, false) }
                            .forEach {
                                response.addHeader(it.name, it.value)
                            }
                        httpResponse.entity?.writeTo(response.outputStream)
                        response.outputStream.flush()
                    } finally {
                        EntityUtils.consumeQuietly(httpResponse.entity)
                    }
                }
            )

        }
    }
}