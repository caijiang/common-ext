package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.AbstractObjectAssert

/**
 * @author CJ
 */
abstract class AbstractJsonNodeObjectAssert<SELF : AbstractObjectAssert<SELF, ObjectNode>>(
    actual: ObjectNode?,
    selfType: Class<*>
) : AbstractJsonNodeAssert<ObjectNode, SELF>(
    actual, selfType
)

@Suppress("unused")
class NormalJsonNodeObjectAssert(actual: ObjectNode?) :
    AbstractJsonNodeObjectAssert<NormalJsonNodeObjectAssert>(actual, NormalJsonNodeObjectAssert::class.java)