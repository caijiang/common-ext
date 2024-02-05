package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode

/**
 * @author CJ
 */
interface BusinessResult {
    val success: Boolean
    val errorCode: String?
    val errorMessage: String?
    val body: JsonNode?
}