package io.github.caijiang.common.test.assertion.rest

import com.fasterxml.jackson.databind.JsonNode
import io.github.caijiang.common.test.assertion.AbstractJsonNodeAssert

/**
 * @author CJ
 */
class RestResourceListAssert(actual: JsonNode?) :
    AbstractJsonNodeAssert<RestResourceListAssert>(
        actual, RestResourceListAssert::class.java
    ) {
    /**
     * @return 获取自身链接
     */
    fun readSelfLink(): String {
        return readLink("self")
    }

    private fun readLink(@Suppress("SameParameterValue") name: String): String {
        val obj = actual["_links"][name]
        return obj["href"].textValue()
    }
}