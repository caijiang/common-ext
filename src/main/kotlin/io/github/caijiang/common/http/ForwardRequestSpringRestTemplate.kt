package io.github.caijiang.common.http

import io.github.caijiang.common.HttpServletRequest
import io.github.caijiang.common.Slf4j.Companion.log
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.util.StreamUtils
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.io.ByteArrayOutputStream

/**
 * @author CJ
 */
@Suppress("unused")
object ForwardRequestSpringRestTemplate {

    @JvmStatic
    fun forward(
        request: HttpServletRequest,
        restTemplate: RestTemplate,
        changer: NewHttpRequestUrl
    ): ResponseEntity<ByteArray> {
        val method = HttpMethod.valueOf(request.method.uppercase())

        val requestHeaders = HttpHeaders()
        val headerNames = request.headerNames
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            if (ForwardRequest.ignoreHeaders(headerName)) {
                continue
            }
            requestHeaders[headerName] = request.getHeader(headerName)
        }
        val body = ByteArrayOutputStream()
        StreamUtils.copy(request.inputStream, body)
        val requestEntity = HttpEntity(body.toByteArray(), requestHeaders)
        val url = changer.newUrl(request)
        log.debug("forwarding request to {}:{}", method, url)
        val entity = restTemplate.exchange<ByteArray>(url, method, requestEntity)

        val headers = HttpHeaders()
        entity.headers.forEach { t, u ->
            if (!ForwardRequest.ignoreHeaders(t, false)) {
                headers.addAll(t, u)
            }
        }
        log.debug("response status:{}", entity.statusCode)
        return ResponseEntity
            .status(entity.statusCode)
            .headers(headers)
            .body(entity.body)
    }

}