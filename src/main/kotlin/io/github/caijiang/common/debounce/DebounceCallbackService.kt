package io.github.caijiang.common.debounce

import java.io.Serializable

/**
 * 被防抖保护的真正业务提供者
 * @author CJ
 */
interface DebounceCallbackService {
    fun invokeBusiness(type: String, arg: Serializable)
}