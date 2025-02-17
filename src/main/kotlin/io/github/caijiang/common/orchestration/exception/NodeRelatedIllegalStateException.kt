package io.github.caijiang.common.orchestration.exception

import io.github.caijiang.common.orchestration.NodeRelatedThrowable
import io.github.caijiang.common.orchestration.ServiceNode

/**
 * @author CJ
 */
class NodeRelatedIllegalStateException(
    override val node: ServiceNode,
    override val message: String? = null,
    override val cause: Throwable? = null
) : IllegalStateException(message, cause), NodeRelatedThrowable