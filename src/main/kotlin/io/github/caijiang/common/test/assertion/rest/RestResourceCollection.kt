package io.github.caijiang.common.test.assertion.rest

import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * @author CJ
 */
data class RestResourceCollection(
    /**
     * _embedded
     */
    val embedded: ObjectNode,
    /**
     * _links
     */
    val links: ObjectNode,
    /**
     *  "size" : 20,
     *     "totalElements" : 0,
     *     "totalPages" : 0,
     *     "number" : 0
     */
    val page: ObjectNode?
)