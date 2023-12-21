package io.github.caijiang.common.http

import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.client.methods.HttpRequestWrapper
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.protocol.HttpContext
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

/**
 * 生成 curl 并且以 `info` 打印在当前类名称的日志中
 * @author CJ
 */
class CurlHttpRequestInterceptor4 : HttpRequestInterceptor {
    private val log = LoggerFactory.getLogger(CurlHttpRequestInterceptor4::class.java)

    companion object {
        val hideHeaders = setOf("Content-Length", "Connection", "Host")
    }

    override fun process(request: HttpRequest, context: HttpContext?) {
        if (!log.isInfoEnabled) return
        try {
            val bodyLines = toBodyLine(request)
            val lines = toHeaderLine(request)
            lines.addAll(bodyLines)

            log.info("\n" + lines.joinToString(separator = " \\\n"))
        } catch (e: Exception) {
            log.warn("", e)
        }
    }

    private fun toHeaderLine(request: HttpRequest): MutableList<String> {
        if (request !is HttpUriRequest) {
            throw IllegalArgumentException("can not write curl for $request")
        }
        if (request !is HttpRequestWrapper) {
            throw IllegalArgumentException("can not write curl for $request")
        }
        val lines = mutableListOf<String>()
        lines.add("curl -k -v --location --request ${request.method} '${request.target.toURI()}${request.uri}'")
        request.allHeaders
            .filter {
                !hideHeaders.contains(it.name)
            }
            .forEach { header ->
                lines.add("--header '${header.name}: ${header.value}'")
            }
        return lines
    }


    private fun toBodyLine(request: HttpRequest?): List<String> {
        if (request !is HttpEntityEnclosingRequest) {
            return emptyList()
        }
        val entity = request.entity ?: return emptyList()

        if (!entity.isRepeatable) {
            throw IllegalArgumentException("HttpEntity 无法重复")
        }
        val buf = ByteArrayOutputStream()
        entity.writeTo(buf)
        buf.flush()

        return listOf(
            "-d '${String(buf.toByteArray())}'"
        )
    }

}