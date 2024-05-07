package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.AbstractListAssert

/**
 * json 数组断言
 * @author CJ
 */
abstract class AbstractJsonNodeArrayAssert<ELE_ASSERT : AbstractJsonNodeAssert<JsonNode, ELE_ASSERT>,
        SELF : AbstractListAssert<SELF, MutableList<JsonNode>, JsonNode, ELE_ASSERT>>(
    actual: MutableList<JsonNode>?,
    selfType: Class<*>
) :
    AbstractListAssert<SELF, MutableList<JsonNode>, JsonNode, ELE_ASSERT>(
        actual, selfType
    )


class NormalJsonNodeArrayAssert(actual: MutableList<JsonNode>?) :
    AbstractJsonNodeArrayAssert<NormalJsonNodeAssert, NormalJsonNodeArrayAssert>(
        actual, NormalJsonNodeArrayAssert::class.java
    ) {
    override fun toAssert(value: JsonNode?, description: String?): NormalJsonNodeAssert {
        return NormalJsonNodeAssert(value)
    }

    override fun newAbstractIterableAssert(iterable: MutableIterable<JsonNode>?): NormalJsonNodeArrayAssert {
        return NormalJsonNodeArrayAssert(iterable?.toMutableList())
    }
}