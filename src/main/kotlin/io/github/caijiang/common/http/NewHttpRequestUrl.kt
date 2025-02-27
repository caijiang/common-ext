package io.github.caijiang.common.http

import io.github.caijiang.common.HttpServletRequest

/**
 * @author CJ
 */
@FunctionalInterface
interface NewHttpRequestUrl {

    /**
     * @param request 原始请求
     * @return 新的完整请求，包含查询参数
     */
    fun newUrl(request: HttpServletRequest): String

}