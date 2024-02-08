package io.github.caijiang.common.test.assertion.rest

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.internal.Objects
import org.slf4j.LoggerFactory

/**
 * @author CJ
 */
class RestResourceCollectionAssert(actual: RestResourceCollection?) :
    AbstractObjectAssert<RestResourceCollectionAssert, RestResourceCollection>(
        actual, RestResourceCollectionAssert::class.java
    ) {
    private val log = LoggerFactory.getLogger(RestResourceCollectionAssert::class.java)

    fun print(): RestResourceCollectionAssert {
        log.info(actual?.toString())
        return this
    }

    /**
     * @return 断言结果数据
     */
    fun asEmbeddedList(): RestResourceListAssert {
        if (actual == null) {
            return RestResourceListAssert(null)
        }
        try {
            val name = actual.embedded.fieldNames().next()
            return RestResourceListAssert(actual.embedded[name]!!.toMutableList())
        } catch (ignored: Exception) {
            return RestResourceListAssert(null)
        }
    }

    /**
     * 断言 total 具体是多少
     */
    fun total(total: Int): RestResourceCollectionAssert {
        Objects.instance().assertEqual(info, actual?.page?.get("totalElements")?.numberValue()?.toInt(), total)
        return this
    }

}