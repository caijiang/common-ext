package io.github.caijiang.common.http


/**
 * @author CJ
 */
internal object ForwardRequest {

    fun ignoreHeaders(headerName: String, forRequest: Boolean = true): Boolean {
        if (forRequest && headerName.equals("Content-Length", ignoreCase = true)) {
            return true
        }
        if (headerName.equals("Host", ignoreCase = true)) {
            return true
        }
        if (headerName.equals("Connection", ignoreCase = true)) {
            return true
        }
        return false
    }

}