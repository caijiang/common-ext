package io.github.caijiang.common.http

import io.github.caijiang.common.HttpServletRequest
import io.github.caijiang.common.HttpServletResponse
import org.apache.http.HttpEntity
import org.apache.http.client.methods.*
import org.apache.http.entity.InputStreamEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils


/**
 * @since 2.2.0
 * @author CJ
 */
@Suppress("unused")
object ForwardRequest4 {

    private fun toHttpEntity(request: HttpServletRequest): HttpEntity? {
        if (request.contentLengthLong == 0L) return null
        return InputStreamEntity(request.inputStream, request.contentLengthLong)
    }

    private fun convert(request: HttpServletRequest, changer: NewHttpRequestUrl): HttpUriRequest {
        val method = request.method
        val url = changer.newUrl(request)
        val httpUriRequest: HttpUriRequest

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
                )
            )
                .use { httpResponse ->
                    try {
                        response.status = httpResponse.statusLine.statusCode
                        httpResponse.allHeaders
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
        }
    }
}