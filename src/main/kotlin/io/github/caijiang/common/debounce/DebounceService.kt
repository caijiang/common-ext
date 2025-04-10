package io.github.caijiang.common.debounce

import java.time.Duration

/**
 * 防抖服务入口
 * @author CJ
 */
interface DebounceService {
    /**
     * 调度一项防抖作业
     * @param type 防抖业务类型
     * @param arg 防抖业务主键
     * @param debounceDuration 防抖时间
     * @param deathDuration 死时间
     * @see DebounceCallbackService.invokeBusiness
     */
    fun debounce(type: String, arg: String, debounceDuration: Duration, deathDuration: Duration)
}