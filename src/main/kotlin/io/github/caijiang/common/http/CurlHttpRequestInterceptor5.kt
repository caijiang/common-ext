package io.github.caijiang.common.http

import org.apache.hc.core5.http.EntityDetails
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.HttpRequest
import org.apache.hc.core5.http.HttpRequestInterceptor
import org.apache.hc.core5.http.protocol.HttpContext
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

/**
 *
 * 生成 curl 并且以 `info` 打印在当前类名称的日志中
 * @author CJ
 */
class CurlHttpRequestInterceptor5 : HttpRequestInterceptor {
    private val log = LoggerFactory.getLogger(CurlHttpRequestInterceptor5::class.java)

    companion object {
        val hideHeaders = setOf("Content-Length", "Connection", "Host")
    }

    override fun process(request: HttpRequest, entity: EntityDetails?, context: HttpContext?) {
        if (!log.isInfoEnabled) return
        try {
            val bodyLines = toBodyLine(entity)
            val lines = mutableListOf<String>()
            lines.add("curl -k -v --location --request ${request.method} '${request.uri}'")
            request.headers
                .filter {
                    !hideHeaders.contains(it.name)
                }
                .forEach { header ->
                    lines.add("--header '${header.name}: ${header.value}'")
                }
            lines.addAll(bodyLines)

            log.info("\n" + lines.joinToString(separator = " \\\n"))
        } catch (e: Exception) {
            log.warn("", e)
        }
    }

    private fun toBodyLine(entity: EntityDetails?): List<String> {
        if (entity == null) {
            return emptyList()
        }

        if (entity is HttpEntity) {
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
        throw IllegalArgumentException("无法处理非 HttpEntity 的请求")
    }
}