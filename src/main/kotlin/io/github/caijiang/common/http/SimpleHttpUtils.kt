package io.github.caijiang.common.http

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.http.SimpleHttpUtils.EntityBuilder
import org.apache.hc.core5.http.ClassicHttpRequest
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.NameValuePair
import org.apache.http.client.methods.HttpUriRequest
import org.springframework.util.ClassUtils
import java.io.File
import java.io.InputStream
import java.io.Serializable

typealias RequestBody = (EntityBuilder) -> Unit

/**
 * 处理简单的 http 访问，这样就可以无视无视环境
 * @author CJ
 */
object SimpleHttpUtils {

    interface EntityBuilder {
        fun setText(text: String)
        fun setBinary(binary: ByteArray)
        fun setStream(stream: InputStream)
        fun setSerializable(serializable: Serializable)
        fun setFile(file: File)
        fun setParameters(vararg parameters: Pair<String, String>)
        fun setContentType(contentType: String)
        fun setContentEncoding(encoding: String)
    }

    @JvmStatic
    fun httpAccess(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        requestBody: RequestBody? = null
    ): SimpleHttpResponse {
        return if (ClassUtils.isPresent("org.apache.hc.client5.http.impl.classic.HttpClientBuilder", null)) {
            org.apache.hc.client5.http.impl.classic.HttpClientBuilder.create()
                .let {
                    if (log.isTraceEnabled) {
                        it.addRequestInterceptorFirst(CurlHttpRequestInterceptor5())
                    } else
                        it
                }
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
                .let {
                    if (log.isTraceEnabled) {
                        it.addInterceptorFirst(CurlHttpRequestInterceptor4())
                    } else
                        it
                }
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

    private fun createHttp4Request(url: String, method: String, requestBody: RequestBody?): HttpUriRequest {
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
                    .let { builder ->
                        var current = builder
                        requestBody(object : EntityBuilder {
                            override fun setText(text: String) {
                                current = current.setText(text)
                            }

                            override fun setBinary(binary: ByteArray) {
                                current = current.setBinary(binary)
                            }

                            override fun setStream(stream: InputStream) {
                                current = current.setStream(stream)
                            }

                            override fun setSerializable(serializable: Serializable) {
                                current = current.setSerializable(serializable)
                            }

                            override fun setFile(file: File) {
                                current = current.setFile(file)
                            }

                            override fun setParameters(vararg parameters: Pair<String, String>) {
                                current = current.setParameters(
                                    (parameters.map {
                                        object : org.apache.http.NameValuePair {
                                            override fun getName(): String = it.first

                                            override fun getValue(): String = it.second
                                        }
                                    })
                                )
                            }

                            override fun setContentType(contentType: String) {
                                current = current.setContentType(org.apache.http.entity.ContentType.parse(contentType))
                            }

                            override fun setContentEncoding(encoding: String) {
                                current = current.setContentEncoding(encoding)
                            }
                        })
                        current
                    }
                    .build()
        }

        return request
    }

    private fun createHttp5Request(url: String, method: String, requestBody: RequestBody?): ClassicHttpRequest {
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
                .let { builder ->
                    var current = builder
                    requestBody(object : EntityBuilder {
                        override fun setText(text: String) {
                            current = current.setText(text)
                        }

                        override fun setBinary(binary: ByteArray) {
                            current = current.setBinary(binary)
                        }

                        override fun setStream(stream: InputStream) {
                            current = current.setStream(stream)
                        }

                        override fun setSerializable(serializable: Serializable) {
                            current = current.setSerializable(serializable)
                        }

                        override fun setFile(file: File) {
                            current = current.setFile(file)
                        }

                        override fun setParameters(vararg parameters: Pair<String, String>) {
                            current = current.setParameters(
                                parameters.map {
                                    object : NameValuePair {
                                        override fun getName(): String = it.first

                                        override fun getValue(): String = it.second
                                    }
                                }
                            )
                        }

                        override fun setContentType(contentType: String) {
                            current = current.setContentType(ContentType.parse(contentType))
                        }

                        override fun setContentEncoding(encoding: String) {
                            current = current.setContentEncoding(encoding)
                        }
                    })
                    current
                }
                .build()
        }
        return request
    }
}