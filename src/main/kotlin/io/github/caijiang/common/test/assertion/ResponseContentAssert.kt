package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.caijiang.common.test.assertion.rest.RestResourceAssert
import io.github.caijiang.common.test.assertion.rest.RestResourceCollection
import io.github.caijiang.common.test.assertion.rest.RestResourceCollectionAssert
import org.assertj.core.api.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.internal.Objects
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity


/**
 * 对于业务结果可以理解为 成功或者失败,业务代码,业务概述,业务数据
 * @author CJ
 */
@Suppress("UNCHECKED_CAST")
open class ResponseContentAssert<
        SELF : ResponseContentAssert<SELF, JSON_ASSERT>,
        JSON_ASSERT : AbstractJsonNodeAssert<JSON_ASSERT>
        >(
    actual: ResponseEntity<String>,
    private val jsonAssertType: Class<JSON_ASSERT>,
    private val business: ToBusinessResult
) : AbstractAssert<SELF, ResponseEntity<String>>(
    actual, ResponseContentAssert::class.java
) {
    private val log = LoggerFactory.getLogger(ResponseContentAssert::class.java)
    private val objectMapper = ObjectMapper()

    @Suppress("MemberVisibilityCanBePrivate")
    protected var businessResult: BusinessResult? = null

    /**
     * 断言唯一值
     * @since 2.4.0
     */
    fun assertSingleHeader(name: String, work: (AbstractStringAssert<*>) -> Unit): SELF {
        val value = actual.headers[name]
        value?.let {
            Objects.instance().assertEqual(info, it.size, 1)
        }
        work(assertThat(value?.firstOrNull()))
        return this as SELF
    }

    /**
     * 断言第一个值
     * @since 2.4.0
     */
    fun assertFirstHeader(name: String, work: (AbstractStringAssert<*>) -> Unit): SELF {
        val value = actual.headers.getFirst(name)
        work(assertThat(value))
        return this as SELF
    }

    /**
     * 断言某一个头
     * @since 2.4.0
     */
    fun assertHeader(name: String, work: (ListAssert<String>) -> Unit): SELF {
        val value = actual.headers[name]
        work(assertThat(value))
        return this as SELF
    }

    /**
     * 断言整体头
     * @since 2.4.0
     */
    fun assertHeaders(work: (MapAssert<String, List<String>>) -> Unit): SELF {
        work(assertThat(actual.headers))
        return this as SELF
    }

    /**
     * @since 2.4.0
     */
    fun workWithHeaders(work: (HttpHeaders) -> Unit): SELF {
        work(actual.headers)
        return this as SELF
    }


    /**
     * @return 这是一个合法的响应
     */
    fun isLegalResponse(): SELF {
//        isNotBlank();
        readAsJson()
        return this as SELF
    }

    /**
     * @return 是个失败的请求
     */
    fun isFailedResponse(): SELF {
        isLegalResponse()
        if (businessResult != null) {
            if (businessResult!!.success) {
                failWithMessage("响应应该不是200，但实际上是, 相关消息:%s", businessResult?.errorMessage)
            }
        }

        return this as SELF
    }

    /**
     * @return 是个成功的请求
     */
    fun isSuccessResponse(): SELF {
        isLegalResponse()
        if (businessResult != null) {
            if (!businessResult!!.success) {
                failWithMessage(
                    "响应不是200，而是%s, 相关消息:%s",
                    businessResult?.errorCode,
                    businessResult?.errorMessage
                )
            }
        }
        return this as SELF
    }

    fun isErrorCodeMatch(code: String): SELF {
        isLegalResponse()
        if (businessResult != null) {
            if (businessResult!!.errorCode != code) {
                failWithMessage("响应 Code 期望: " + code + ", 实际: " + businessResult!!.errorCode)
            }
        }
        return this as SELF
    }

    fun print(): SELF {
        isLegalResponse()
        businessResult?.let {
            println("success:${it.success}")
            println("body:${it.body}")
            println("errorCode:${it.errorCode}")
            println("errorMessage:${it.errorMessage}")
        }
        return this as SELF
    }

    /**
     * @return 断言业务结果是数组
     */
    fun isListResponse(): SELF {
        isSuccessResponse()
        if (businessResult != null) {
            if (businessResult!!.body?.isArray != true) {
                failWithMessage(
                    "结果数据:%s 不是List",
                    businessResult?.body
                )
            }
        }
        return this as SELF
    }

    /**
     * @return 转成数组断言
     */
    fun asListAssert(): JSON_ASSERT {
        isListResponse()
        return jsonAssertType.kotlin.constructors
            .find { it.parameters.size == 1 && it.parameters[0].type.classifier == JsonNode::class }
            ?.call(businessResult?.body) ?: throw IllegalArgumentException("no constructors find for $jsonAssertType¬")
    }

    /**
     * @return 断言业务数据是对象
     */
    fun isObjectResponse(): SELF {
        isSuccessResponse()
        if (businessResult != null) {
            if (businessResult!!.body?.isObject != true) {
                failWithMessage(
                    "结果数据:%s 不是Object",
                    businessResult?.body
                )
            }
        }
        return this as SELF
    }

    /**
     * @return 转成对象断言
     */
    fun asObject(): JSON_ASSERT {
        isObjectResponse()
        return jsonAssertType.kotlin.constructors
            .find { it.parameters.size == 1 && it.parameters[0].type.classifier == JsonNode::class }
            ?.call(businessResult?.body) ?: throw IllegalArgumentException("no constructors find for $jsonAssertType¬")
    }

    /**
     * 失败了也可以有结果
     * @since 2.0.0
     * @see JsonNode
     * @return 转成JsnoNode断言
     */
    fun asJsonNodeAssert(): JSON_ASSERT {
        isLegalResponse()
        return jsonAssertType.kotlin.constructors
            .find { it.parameters.size == 1 && it.parameters[0].type.classifier == JsonNode::class }
            ?.call(businessResult?.body) ?: throw IllegalArgumentException("no constructors find for $jsonAssertType¬")
    }

    /**
     * @return 断言业务数据是 null
     * @see 1.2.0
     */
    fun isNullResponse(): SELF {
        isLegalResponse()
        if (businessResult != null) {
            // body 不是 null 而且 is Null 也不是
            if (businessResult!!.body != null) {
                if (businessResult!!.body?.isNull != true) {
                    failWithMessage(
                        "结果数据:%s 不是null",
                        businessResult?.body
                    )
                }
            }

        }
        return this as SELF
    }


    private fun JsonNode?.findObjectNodeChild(childName: String): ObjectNode {
        val child = this?.get(childName)
        if (child == null || child.isNull) {
            return objectMapper.createObjectNode()
        }
        if (child.isObject) return child as ObjectNode
        throw IllegalArgumentException("child `$childName` is not an object; ${child.nodeType}")
    }

    /**
     * @return 转成 spring rest 资源集合断言
     */
    fun asSpringRestCollection(): RestResourceCollectionAssert {
        isSuccessResponse()
        if (businessResult != null) {
            return RestResourceCollectionAssert(
                RestResourceCollection(
                    businessResult?.body?.findObjectNodeChild("_embedded")!!,
                    businessResult?.body?.findObjectNodeChild("_links")!!,
                    businessResult!!.body?.get("page") as ObjectNode?,
                )
            )
        }
        return RestResourceCollectionAssert(null)
    }

    /**
     * @return 转成 spring rest 单个资源断言
     */
    fun asSpringRest(): RestResourceAssert {
        isSuccessResponse()
        if (businessResult != null) {
            return RestResourceAssert(businessResult?.body)
        }
        return RestResourceAssert(null)
    }

    /**
     * 返回数据结果
     *
     * @return 返回数据结果
     * @author Guomw 2023/6/16 17:29
     */
    fun <T> readData(javaClass: Class<T>): T? {
        isSuccessResponse()
        if (businessResult != null) {
            if (businessResult!!.body == null) {
                return null
            }
            val reader = objectMapper.readerFor(javaClass)
            return reader.readValue(businessResult!!.body)
        }
        return null
    }

    inline fun <reified T> readData(): T? {
        return readData(T::class.java)
    }

    /**
     * @return 关于实际结果的新断言
     */
    fun <T> asData(javaClass: Class<T>): ObjectAssert<T?> {
        val rs = readData(javaClass)
        return assertThat(rs)
    }

    fun <T> readFromData(reader: (BusinessResult) -> T?): T? {
        isSuccessResponse()
        if (businessResult == null) return null
        return reader(businessResult!!)
    }

    fun <T> readFromEntity(reader: (ResponseEntity<String>) -> T): T {
        return reader(actual)
    }

    private fun readAsJson() {
        if (businessResult != null)
            return
        try {
            // 如果 是个json 而且携带有 resultCode resultMsg data
            businessResult = business.call(actual)
        } catch (e: Exception) {
            log.warn("failed", e)
            failWithMessage("期望是一个合法的json object，实际确实:%s", actual)
        }
    }

}

class NormalResponseContentAssert(actual: ResponseEntity<String>, business: ToBusinessResult) :
    ResponseContentAssert<NormalResponseContentAssert, NormalJsonNodeAssert>(
        actual, NormalJsonNodeAssert::class.java, business
    )