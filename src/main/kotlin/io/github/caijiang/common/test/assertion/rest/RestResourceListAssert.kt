package io.github.caijiang.common.test.assertion.rest

import com.fasterxml.jackson.databind.JsonNode
import io.github.caijiang.common.test.assertion.AbstractJsonNodeArrayAssert

/**
 * @author CJ
 */
class RestResourceListAssert(actual: MutableList<JsonNode>?) :
    AbstractJsonNodeArrayAssert<RestResourceAssert, RestResourceListAssert>(
        actual, RestResourceListAssert::class.java
    ) {
    override fun toAssert(value: JsonNode?, description: String?): RestResourceAssert {
        return RestResourceAssert(value)
    }

    override fun newAbstractIterableAssert(iterable: MutableIterable<JsonNode>?): RestResourceListAssert {
        return RestResourceListAssert(iterable?.toMutableList())
    }
}