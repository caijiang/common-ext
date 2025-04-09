package io.github.caijiang.common.debounce.bean

import io.github.caijiang.common.debounce.DebounceService
import io.github.caijiang.common.debounce.DelayMQData
import io.github.caijiang.common.debounce.MqSender
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.io.Serializable
import java.time.Duration
import java.util.*

/**
 * @author CJ
 */
@Service
class DebounceServiceImpl(
    private val applicationContext: ApplicationContext,
//    private val mqSender: MqSender,
    private val debounceRedisTemplate: RedisTemplate<String, Any?>,
) : DebounceService {
    private var mqSender: MqSender? = null
    private val debounceAbsent: RedisScript<String?> = RedisScript.of(
        ClassPathResource("/lua/debounce-absent.lua"),
        String::class.java
    )

//    @PostConstruct
//    fun init() {
//        mqSender = applicationContext.getBean(MqSender::class.java)
//    }

    /**
     * 用原子方式写入业务 id
     * @return 是否新增，和事务 id
     */
    private fun saveOrQueryTraceId(type: String, arg: Serializable): Pair<Boolean, UUID> {
        val key = DelayMQData.redisHashKeyFor(type, arg)
        val inputTrace = UUID.randomUUID()
        @Suppress("USELESS_CAST") val resultTrace =
            debounceRedisTemplate.execute(debounceAbsent, listOf(key), inputTrace.toString()) as String?
                ?: return Pair(true, inputTrace)
        return Pair(false, UUID.fromString(resultTrace))
    }

    override fun debounce(type: String, arg: Serializable, debounceDuration: Duration, deathDuration: Duration) {
        if (mqSender == null) {
            mqSender = applicationContext.getBean(MqSender::class.java)
        }
        val (created, id) = saveOrQueryTraceId(type, arg)

        if (created) {
            val message = MessageBuilder
                .withPayload(
                    DelayMQData(type, arg, id, null),
                ).setHeaderIfAbsent("common-debounce", true)
                .build()
            mqSender?.sendDelay(message, deathDuration)
        }
        val mqWithDebounce = DelayMQData(type, arg, id, System.currentTimeMillis() + debounceDuration.toMillis())

        val hash = debounceRedisTemplate.opsForHash<String, Any?>()
        hash.put(mqWithDebounce.redisHashKey(), "debounce", mqWithDebounce.debounceTimestamp!!)
        val message = MessageBuilder.withPayload(
            mqWithDebounce,
        ).setHeaderIfAbsent("common-debounce", true)
            .build()
        mqSender?.sendDelay(message, debounceDuration)
    }
}