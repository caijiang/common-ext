package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.StringUtils

/**
 * @author CJ
 */
@FunctionalInterface
interface ToBusinessResult {
    fun call(entity: ResponseEntity<String>): BusinessResult
}


object StdToBusinessResult : ToBusinessResult {
    private val objectMapper = ObjectMapper()
    override fun call(entity: ResponseEntity<String>): BusinessResult {
        val success = entity.statusCode.is2xxSuccessful
        val message = entity.toString()
        val body = if (StringUtils.hasText(entity.body)) readyBody(entity) else null
        return object : BusinessResult {
            override val success: Boolean
                get() = success
            override val errorCode: String
                get() = entity.statusCodeValue.toString()
            override val errorMessage: String
                get() = message
            override val body: JsonNode?
                get() = body
        }
    }

    private fun readyBody(entity: ResponseEntity<String>): JsonNode {
        if (entity.headers.contentType?.isCompatibleWith(MediaType.APPLICATION_JSON) == true
            || entity.headers.contentType?.isCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) == true
        ) {
            return objectMapper.readTree(entity.body)
        }
        if (entity.headers.contentType?.isCompatibleWith(MediaType.TEXT_PLAIN) == true) {
            return TextNode.valueOf(entity.body)
        }
        throw IllegalStateException("unsupported contentType:${entity.headers.contentType}")
    }

}