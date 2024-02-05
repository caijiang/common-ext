package io.github.caijiang.common.test.assertion

import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.caijiang.common.test.assertion.rest.RestResourceCollection
import io.github.caijiang.common.test.assertion.rest.RestResourceCollectionAssert
import org.assertj.core.api.AbstractAssert
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity


/**
 * 对于业务结果可以理解为 成功或者失败,业务代码,业务概述,业务数据
 * @author CJ
 */
class ResponseContentAssert(
    actual: ResponseEntity<String>,
    private val business: ToBusinessResult
) : AbstractAssert<ResponseContentAssert, ResponseEntity<String>>(
    actual, ResponseContentAssert::class.java
) {
    private val log = LoggerFactory.getLogger(ResponseContentAssert::class.java)


    private var businessResult: BusinessResult? = null

    /**
     * @return 这是一个合法的响应
     */
    fun isLegalResponse(): ResponseContentAssert {
//        isNotBlank();
        readAsJson()
        return this
    }

    /**
     * @return 是个失败的请求
     */
    fun isFailedResponse(): ResponseContentAssert {
        isLegalResponse()
        if (businessResult != null) {
            if (businessResult!!.success) {
                failWithMessage("响应应该不是200，但实际上是, 相关消息:%s", businessResult?.errorMessage)
            }
        }

        return this
    }

    /**
     * @return 是个成功的请求
     */
    fun isSuccessResponse(): ResponseContentAssert {
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
        return this
    }

    fun isErrorCodeMatch(code: String): ResponseContentAssert {
        isLegalResponse()
        if (businessResult != null) {
            if (businessResult!!.errorCode != code) {
                failWithMessage("响应 Code 期望: " + code + ", 实际: " + businessResult!!.errorCode)
            }
        }
        return this
    }

    fun asSpringRestCollection(): RestResourceCollectionAssert {
        isSuccessResponse()
        if (businessResult != null) {
            return RestResourceCollectionAssert(
                RestResourceCollection(
                    businessResult!!.body!!["_embedded"] as ObjectNode,
                    businessResult!!.body!!["_links"] as ObjectNode,
                    businessResult!!.body?.get("page") as ObjectNode?,
                )
            )
        }
        return RestResourceCollectionAssert(null)
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