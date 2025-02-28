package io.github.caijiang.common.http

import io.github.caijiang.common.HttpServletRequest
import io.github.caijiang.common.Slf4j.Companion.log
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.util.StreamUtils
import java.io.ByteArrayOutputStream
import java.net.URI

/**
 * @author CJ
 */
@Suppress("unused")
object ForwardRequestSpringClientHttpRequestFactory {

    @JvmStatic
    fun forward(
        request: HttpServletRequest,
        factory: ClientHttpRequestFactory,
        changer: NewHttpRequestUrl
    ): ResponseEntity<ByteArray> {
        val method = HttpMethod.valueOf(request.method.uppercase())

        val url = changer.newUrl(request)
        log.debug("forwarding request to {}:{}", method, url)
        val clientRequest = factory.createRequest(URI(url), method)

        val headerNames = request.headerNames
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            if (ForwardRequest.ignoreHeaders(headerName)) {
                continue
            }
            clientRequest.headers.add(headerName, request.getHeader(headerName))
        }
        StreamUtils.copy(request.inputStream, clientRequest.body)

        return clientRequest.execute()
            .use { response ->
                log.debug("response status:{}", response.statusCode)
                val headers = HttpHeaders()
                response.headers.forEach { t, u ->
                    if (!ForwardRequest.ignoreHeaders(t, false)) {
                        headers.addAll(t, u)
                    }
                }
                val buffer = ByteArrayOutputStream()
                StreamUtils.copy(response.body, buffer)
                ResponseEntity.status(response.statusCode)
                    .headers(headers)
                    .body(buffer.toByteArray())
            }
    }
}