package io.github.caijiang.common.http

/**
 * @author CJ
 */
data class SimpleHttpResponse(
    val status: Int,
    val headers: Map<String, String>,
    val body: ByteArray?,
)
