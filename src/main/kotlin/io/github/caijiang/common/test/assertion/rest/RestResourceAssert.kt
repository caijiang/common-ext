package io.github.caijiang.common.test.assertion.rest

import com.fasterxml.jackson.databind.JsonNode
import io.github.caijiang.common.test.assertion.AbstractJsonNodeAssert

class RestResourceAssert(actual: JsonNode?) :
    AbstractJsonNodeAssert<RestResourceAssert>(actual, RestResourceAssert::class.java) {

    /**
     * @return 获取自身链接
     */
    @Suppress("unused")
    fun readSelfLink(): String {
        return readLink("self")
    }

    private fun readLink(@Suppress("SameParameterValue") name: String): String {
        val obj = actual["_links"][name]
        return obj["href"].textValue()
    }

}