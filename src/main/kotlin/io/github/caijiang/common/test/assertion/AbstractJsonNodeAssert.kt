package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.AbstractObjectAssert

/**
 * @author CJ
 */
abstract class AbstractJsonNodeAssert<NODE : JsonNode, SELF : AbstractObjectAssert<SELF, NODE>>(
    actual: NODE?,
    selfType: Class<*>
) :
    AbstractObjectAssert<SELF, NODE>(
        actual, selfType
    )


class NormalJsonNodeAssert(actual: JsonNode?) :
    AbstractJsonNodeAssert<JsonNode, NormalJsonNodeAssert>(actual, NormalJsonNodeAssert::class.java)