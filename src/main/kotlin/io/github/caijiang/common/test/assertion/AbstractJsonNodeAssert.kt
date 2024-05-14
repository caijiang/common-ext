package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import org.assertj.core.api.AbstractListAssert
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Condition
import org.assertj.core.api.EnumerableAssert
import org.assertj.core.api.filter.FilterOperator
import org.assertj.core.internal.Objects
import java.math.BigDecimal
import java.math.BigInteger
import java.util.function.Predicate

private val objectMapper = ObjectMapper()

/**
 * @param AT 元素断言的类型
 */
@Suppress("UNCHECKED_CAST")
class FakeJsonArrayAssert<AT : AbstractJsonNodeAssert<AT>>(
    actual: MutableList<JsonNode>?,
    private val assertType: Class<*>,
) :
    AbstractListAssert<FakeJsonArrayAssert<AT>, MutableList<JsonNode>, JsonNode, AT>(
        actual,
        FakeJsonArrayAssert::class.java
    ) {
    override fun toAssert(value: JsonNode?, description: String?): AT {
        return (assertType.kotlin.constructors
            .find { it.parameters.size == 1 && it.parameters[0].type.classifier == JsonNode::class }
            ?.call(value) ?: throw IllegalArgumentException("no constructors find for :$assertType")) as AT
    }

    override fun newAbstractIterableAssert(iterable: MutableIterable<JsonNode>?): FakeJsonArrayAssert<AT> {
        return FakeJsonArrayAssert(iterable?.toMutableList(), assertType)
    }
//
//    fun back():AT{
//        return assertInstance
//    }
}

/**
 * 从代码技巧上实现 [AbstractListAssert]
 * @author CJ
 */
@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
abstract class AbstractJsonNodeAssert<SELF : AbstractJsonNodeAssert<SELF>>(
    actual: JsonNode?,
    selfType: Class<*>,
    protected val innerListAssertHelper: FakeJsonArrayAssert<SELF> = FakeJsonArrayAssert(
        actual?.toMutableList(),
        selfType
    )
) :
    AbstractObjectAssert<SELF, JsonNode>(
        actual, selfType
    ), EnumerableAssert<FakeJsonArrayAssert<SELF>, JsonNode> by innerListAssertHelper {
    fun first(): SELF = innerListAssertHelper.first()
    fun last(): SELF = innerListAssertHelper.last()
    fun filteredOn(propertyOrFieldName: String, expectedValue: Any): FakeJsonArrayAssert<SELF> =
        innerListAssertHelper.filteredOn(propertyOrFieldName, expectedValue)

    fun filteredOn(condition: Condition<in JsonNode>): FakeJsonArrayAssert<SELF> =
        innerListAssertHelper.filteredOn(condition)

    fun filteredOn(predicate: Predicate<in JsonNode>): FakeJsonArrayAssert<SELF> =
        innerListAssertHelper.filteredOn(predicate)

    fun filteredOn(propertyOrFieldName: String, filterOperator: FilterOperator<*>): FakeJsonArrayAssert<SELF> =
        innerListAssertHelper.filteredOn(propertyOrFieldName, filterOperator)

    private val objects = Objects.instance()

    fun print(): SELF {
        println(actual?.toString())
        return this as SELF
    }

    /**
     * 断言 path 所在的类型为输入值
     */
    fun hasThisType(expected: JsonNodeType, optional: Boolean, path: String): SELF {
        return hasThisType(expected, optional, {
            it[path]
        }, path)
    }

    /**
     * 断言 path 所在的类型为输入值
     * @param path 多重 path, 比如 "children",2   表示children里的第三个
     */
    fun hasThisType(expected: JsonNodeType, optional: Boolean, vararg path: Any): SELF {
        return hasThisType(expected, optional, path.toToNodeFunc(), path.toFieldDescription())
    }

    /**
     * 断言 path 所在的类型为输入值
     * @param toNode 从当前 node 到目标 node
     */
    fun hasThisType(
        expected: JsonNodeType,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        info.description("field:%s", fieldDescription)
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
        return hasNumberNode(expected, optional, {
            it[path]
        }, path)
    }

    fun hasNumberNode(expected: Number, optional: Boolean, vararg path: Any): SELF {
        return hasNumberNode(expected, optional, path.toToNodeFunc(), path.toFieldDescription())
    }

    fun hasNumberNode(
        expected: Number,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        info.description("field:%s", fieldDescription)
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
        return hasTextNode(expected, optional, { it[path] }, path)
    }

    fun hasTextNode(expected: String, optional: Boolean, vararg path: Any): SELF {
        return hasTextNode(expected, optional, path.toToNodeFunc(), path.toFieldDescription())
    }

    fun hasTextNode(
        expected: String,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        val a = actual?.let(toNode)
        info.description("field:%s", fieldDescription)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.textValue(), expected)
        }
        return this as SELF
    }

    fun hasTextNode(expected: Predicate<String>, optional: Boolean, path: String): SELF {
        return hasTextNode(expected, optional, { it[path] }, path)
    }

    fun hasTextNode(expected: Predicate<String>, optional: Boolean, vararg path: Any): SELF {
        return hasTextNode(expected, optional, path.toToNodeFunc(), path.toFieldDescription())
    }

    fun hasTextNode(
        expected: Predicate<String>,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        val a = actual?.let(toNode)
        info.description("field:%s", fieldDescription)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, expected.test(it.textValue()), true)
        }
        return this as SELF
    }

    fun hasBigIntegerNode(expected: BigInteger, optional: Boolean, path: String): SELF {
        return hasBigIntegerNode(expected, optional, { it[path] }, path)
    }

    fun hasBigIntegerNode(expected: BigInteger, optional: Boolean, vararg path: Any): SELF {
        return hasBigIntegerNode(expected, optional, path.toToNodeFunc(), path.toFieldDescription())
    }

    fun hasBigIntegerNode(
        expected: BigInteger,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        info.description("field:%s", fieldDescription)
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
        return hasDecimalNode(expected, optional, { it[path] }, path)
    }

    fun hasDecimalNode(expected: BigDecimal, optional: Boolean, vararg path: Any): SELF {
        return hasDecimalNode(expected, optional, path.toToNodeFunc(), path.toFieldDescription())
    }

    fun hasDecimalNode(
        expected: BigDecimal,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        info.description("field:%s", fieldDescription)
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
        return hasBooleanNode(expected, optional, { it[path] }, path)
    }

    fun hasBooleanNode(expected: Boolean, optional: Boolean, vararg path: Any): SELF {
        return hasBooleanNode(expected, optional, path.toToNodeFunc(), path.toFieldDescription())
    }

    fun hasBooleanNode(
        expected: Boolean,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        info.description("field:%s", fieldDescription)
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
        return hasBinaryNode(expected, optional, { it[path] }, path)
    }

    fun hasBinaryNode(expected: ByteArray, optional: Boolean, vararg path: Any): SELF {
        return hasBinaryNode(expected, optional, path.toToNodeFunc(), path.toFieldDescription())
    }

    fun hasBinaryNode(
        expected: ByteArray,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        info.description("field:%s", fieldDescription)
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.binaryValue(), expected)
        }
        return this as SELF
    }

    fun hasArrayNodeLength(expected: Int, optional: Boolean, path: String): SELF {
        return hasArrayNodeLength(expected, optional, { it[path] }, "")
    }

    fun hasArrayNodeLength(expected: Int, optional: Boolean, vararg path: Any): SELF {
        return hasArrayNodeLength(expected, optional, path.toToNodeFunc(), "")
    }

    fun hasArrayNodeLength(
        expected: Int,
        optional: Boolean,
        toNode: (JsonNode) -> JsonNode?,
        fieldDescription: String
    ): SELF {
        hasThisType(JsonNodeType.ARRAY, optional, toNode, fieldDescription)
        info.description("field:%s", fieldDescription)
        val a = actual?.let(toNode)
        if (!optional) {
            objects.assertNotNull(info, a)
        }
        a?.let {
            objects.assertEqual(info, it.size(), expected)
        }
        return this as SELF
    }

    // 转成新的断言，一种是普通的，一种是对象，一种是数组
    /**
     * @return 断言关联字段
     */
    fun assertData(vararg path: Any): SELF {
        return assertData(path.toToNodeFunc())
    }

    /**
     * @return 断言关联字段
     */
    fun assertData(toNode: (JsonNode) -> JsonNode? = { it }): SELF {
        val rs = toNode(actual)
        val javaClass = myself.javaClass

        // 2
        return javaClass.kotlin.constructors.find {
            it.parameters.size == 2
                    && it.parameters[0].type.classifier == JsonNode::class
                    && it.parameters[1].type.classifier == Class::class
        }?.call(rs, javaClass)
            ?: javaClass.kotlin.constructors.find {
                it.parameters.size == 1
                        && it.parameters[0].type.classifier == JsonNode::class
            }?.call(rs)
            ?: throw IllegalArgumentException("No constructor found for ${javaClass.name}")

    }

    /**
     * @return 断言当前的类型
     */
    fun thisTypeEqualTo(type: JsonNodeType): SELF {
        objects.assertEqual(info, actual.nodeType, type)
        return this as SELF
    }

    /**
     * 返回数据结果
     *
     * @return 返回数据结果
     * @author Guomw 2023/6/16 17:29
     */
    fun <T> readData(javaClass: Class<T>, path: String): T? {
        return readData(javaClass) { it[path] }
    }

    /**
     * 返回数据结果
     *
     * @return 返回数据结果
     * @author Guomw 2023/6/16 17:29
     */
    fun <T> readData(javaClass: Class<T>, vararg path: Any): T? {
        return readData(javaClass, path.toToNodeFunc())
    }

    /**
     * 返回数据结果
     *
     * @return 返回数据结果
     * @author Guomw 2023/6/16 17:29
     */
    fun <T> readData(
        javaClass: Class<T>,
        toNode: (JsonNode) -> JsonNode? = { it },
    ): T? {
        val a = actual?.let(toNode) ?: return null
        val reader = objectMapper.readerFor(javaClass)
        return reader.readValue<T>(a)
    }

}

private fun <T> Array<T>.toFieldDescription(): String {
    return this.joinToString { it.toString() }
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
    AbstractJsonNodeAssert<NormalJsonNodeAssert>(actual, NormalJsonNodeAssert::class.java)