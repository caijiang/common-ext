package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.AbstractListAssert

/**
 * @author CJ
 */
abstract class AbstractJsonNodeArrayAssert<SELF : AbstractListAssert<SELF, MutableList<JsonNode>, JsonNode, NormalJsonNodeAssert>>(
    actual: MutableList<JsonNode>?,
    selfType: Class<*>
) :
    AbstractListAssert<SELF, MutableList<JsonNode>, JsonNode, NormalJsonNodeAssert>(
        actual, selfType
    ) {
    override fun toAssert(value: JsonNode?, desc: String?): NormalJsonNodeAssert {
        return NormalJsonNodeAssert(value)
    }

}

@Suppress("unused")
class NormalJsonNodeArrayAssert(actual: MutableList<JsonNode>?) :
    AbstractJsonNodeArrayAssert<NormalJsonNodeArrayAssert>(
        actual, NormalJsonNodeArrayAssert::class.java
    ) {
    override fun newAbstractIterableAssert(iterable: MutableIterable<JsonNode>?): NormalJsonNodeArrayAssert {
        return NormalJsonNodeArrayAssert(iterable?.toMutableList())
    }
}