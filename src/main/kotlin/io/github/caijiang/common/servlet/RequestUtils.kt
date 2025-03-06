package io.github.caijiang.common.servlet

import io.github.caijiang.common.HttpServletRequest
import io.github.caijiang.common.Slf4j.Companion.log
import org.springframework.util.Base64Utils
import org.springframework.util.CollectionUtils

/**
 * @since 2.3.0
 * @author CJ
 */
object RequestUtils {
    @JvmStatic
    fun logInSlf(request: HttpServletRequest) {
        log.info("### {} request {}:{}", request.remoteAddr, request.method, request.requestURI)
        log.info("### queryString: {}", request.queryString)
        val headerNames = request.headerNames
        log.info("#########   request headers:<<<<<")
        while (headerNames.hasMoreElements()) {
            val headerName = headerNames.nextElement()
            val values = request.getHeaders(headerName)
            log.info("{}:{}", headerName, CollectionUtils.toIterator(values).asSequence().joinToString("|"))
        }
        log.info(">>>>> request headers #########")
        log.info("######### request body(use Base64Utils.decodeFromString())<<<<<:")
        val data = request.inputStream.readBytes()
        log.info("{}", Base64Utils.encodeToString(data))
        log.info(">>>>> request body #########")
    }
}

fun HttpServletRequest.logInSlf() {
    RequestUtils.logInSlf(this)
}