package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.internal.Objects
import java.math.BigDecimal
import java.math.BigInteger

/**
 * 本来是想设计为所有 JsonNode 的基类，但明显遇到了困难
 * @author CJ
 */
@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
abstract class AbstractJsonNodeAssert<NODE : JsonNode, SELF : AbstractObjectAssert<SELF, NODE>>(
    actual: NODE?,
    selfType: Class<*>
) :
    AbstractObjectAssert<SELF, NODE>(
        actual, selfType
    ) {
    private val objects = Objects.instance()

    fun print(): SELF {
        println(actual?.toString())
        return this as SELF
    }

    /**
     * 断言 path 所在的类型为输入值
     */
    fun hasThisType(expected: JsonNodeType, optional: Boolean, path: String): SELF {
        return hasThisType(expected, optional) {
            it[path]
        }
    }

    /**
     * 断言 path 所在的类型为输入值
     * @param path 多重 path, 比如 "children",2   表示children里的第三个
     */
    fun hasThisType(expected: JsonNodeType, optional: Boolean, vararg path: Any): SELF {
        return hasThisType(expected, optional, path.toToNodeFunc())
    }

    /**
     * 断言 path 所在的类型为输入值
     * @param toNode 从当前 node 到目标 node
     */
    fun hasThisType(expected: JsonNodeType, optional: Boolean, toNode: (JsonNode) -> JsonNode?): SELF {
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.nodeType, expected)
        }
        return this as SELF
    }

    fun hasNumberNode(expected: Number, optional: Boolean, path: String): SELF {
        return hasNumberNode(expected, optional) {
            it[path]
        }
    }

    fun hasNumberNode(expected: Number, optional: Boolean, vararg path: Any): SELF {
        return hasNumberNode(expected, optional, path.toToNodeFunc())
    }

    fun hasNumberNode(expected: Number, optional: Boolean, toNode: (JsonNode) -> JsonNode?): SELF {
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.numberValue(), expected)
        }
        return this as SELF
    }

    fun hasTextNode(expected: String, optional: Boolean, path: String): SELF {
        return hasTextNode(expected, optional) { it[path] }
    }

    fun hasTextNode(expected: String, optional: Boolean, vararg path: Any): SELF {
        return hasTextNode(expected, optional, path.toToNodeFunc())
    }

    fun hasTextNode(expected: String, optional: Boolean, toNode: (JsonNode) -> JsonNode?): SELF {
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.textValue(), expected)
        }
        return this as SELF
    }

    fun hasBigIntegerNode(expected: BigInteger, optional: Boolean, path: String): SELF {
        return hasBigIntegerNode(expected, optional) { it[path] }
    }

    fun hasBigIntegerNode(expected: BigInteger, optional: Boolean, vararg path: Any): SELF {
        return hasBigIntegerNode(expected, optional, path.toToNodeFunc())
    }

    fun hasBigIntegerNode(expected: BigInteger, optional: Boolean, toNode: (JsonNode) -> JsonNode?): SELF {
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.bigIntegerValue(), expected)
        }
        return this as SELF
    }

    fun hasDecimalNode(expected: BigDecimal, optional: Boolean, path: String): SELF {
        return hasDecimalNode(expected, optional) { it[path] }
    }

    fun hasDecimalNode(expected: BigDecimal, optional: Boolean, vararg path: Any): SELF {
        return hasDecimalNode(expected, optional, path.toToNodeFunc())
    }

    fun hasDecimalNode(expected: BigDecimal, optional: Boolean, toNode: (JsonNode) -> JsonNode?): SELF {
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.decimalValue(), expected)
        }
        return this as SELF
    }

    fun hasBooleanNode(expected: Boolean, optional: Boolean, path: String): SELF {
        return hasBooleanNode(expected, optional) { it[path] }
    }

    fun hasBooleanNode(expected: Boolean, optional: Boolean, vararg path: Any): SELF {
        return hasBooleanNode(expected, optional, path.toToNodeFunc())
    }

    fun hasBooleanNode(expected: Boolean, optional: Boolean, toNode: (JsonNode) -> JsonNode?): SELF {
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.booleanValue(), expected)
        }
        return this as SELF
    }

    fun hasBinaryNode(expected: ByteArray, optional: Boolean, path: String): SELF {
        return hasBinaryNode(expected, optional) { it[path] }
    }

    fun hasBinaryNode(expected: ByteArray, optional: Boolean, vararg path: Any): SELF {
        return hasBinaryNode(expected, optional, path.toToNodeFunc())
    }

    fun hasBinaryNode(expected: ByteArray, optional: Boolean, toNode: (JsonNode) -> JsonNode?): SELF {
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.binaryValue(), expected)
        }
        return this as SELF
    }

}

private fun <T : Any> Array<T>.toToNodeFunc(): (JsonNode) -> JsonNode? {
    return {
        var current = it
        for (p in this) {
            current = when (p) {
                is Int -> {
                    current.get(p)
                }

                is String -> {
                    current.get(p)
                }

                else -> throw IllegalArgumentException("Unsupported $p(${p.javaClass}) in Json Path")
            }
        }
        current
    }
}


class NormalJsonNodeAssert(actual: JsonNode?) :
    AbstractJsonNodeAssert<JsonNode, NormalJsonNodeAssert>(actual, NormalJsonNodeAssert::class.java)