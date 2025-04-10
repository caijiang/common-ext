package io.github.caijiang.common.debounce.bean

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.debounce.DebounceCallbackService
import io.github.caijiang.common.debounce.DelayMQData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

/**
 * @author CJ
 */
@Component
class MqMessageHandler(
    @Qualifier("debounceRedisTemplate")
    private val debounceRedisTemplate: RedisTemplate<String, Any>,
    private val debounceCallbackService: DebounceCallbackService,
) {
    fun handleIt(message: DelayMQData) {
        val hash = debounceRedisTemplate.opsForHash<String, Any?>()
        val key = message.redisHashKey()

        // 获取目标 trace id
        val currentTrace = hash.get(key, "trace")
        log.debug("received debounce callback, message:{},currentTrace:{},hash:{}", message, currentTrace, key)
        if (currentTrace == null || currentTrace != message.id.toString()) {
            return
        }
        if (message.debounceTimestamp != null) {
            // 防抖mq
            val debounce = hash.get("debounce:${message.type}:${message.arg}", "debounce") as Long?
            log.debug("that's not death call, debounce:{}", debounce)
            if (debounce != null && debounce != message.debounceTimestamp) {
                return
            }
        }

        if (debounceRedisTemplate.delete(key)) {
            debounceCallbackService.invokeBusiness(message.type, message.arg)
        }
    }
}