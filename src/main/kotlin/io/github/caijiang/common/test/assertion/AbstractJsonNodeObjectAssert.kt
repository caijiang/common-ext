package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.AbstractObjectAssert

/**
 * 这个是为 JsonObject 设计的断言
 * @see ObjectNode
 * @author CJ
 */
abstract class AbstractJsonNodeObjectAssert<SELF : AbstractObjectAssert<SELF, JsonNode>>(
    actual: JsonNode?,
    selfType: Class<*>
) : AbstractJsonNodeAssert<JsonNode, SELF>(
    actual, selfType
)

class NormalJsonNodeObjectAssert(actual: JsonNode?) :
    AbstractJsonNodeObjectAssert<NormalJsonNodeObjectAssert>(actual, NormalJsonNodeObjectAssert::class.java)