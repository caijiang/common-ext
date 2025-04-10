package io.github.caijiang.common.debounce

import io.github.caijiang.common.Slf4j.Companion.log
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import java.nio.ByteBuffer


/**
 * @author CJ
 */
internal open class DebounceConfiguration(
    private val redisConnectionFactory: RedisConnectionFactory,
) {

    @Bean("debounceRedisTemplate")
    open fun debounceRedisTemplate(): RedisTemplate<String, Any?> {
        val x = RedisSerializer.string()
        // value 有 2 种,string 跟 long; string 是通过 redis 直接写的 没有机会下手，long 我就定义用 0x00 识别
        val valueSerializer: RedisSerializer<Any?> = object : RedisSerializer<Any?> {
            override fun serialize(t: Any?): ByteArray? {
                val value = t ?: return null
                if (t is String) {
                    return x.serialize(t)
                }
                val buffer = ByteBuffer.allocate(9)
                buffer.put(0x00)
                buffer.putLong(value as Long)
                return buffer.array()
            }

            override fun deserialize(bytes: ByteArray?): Any? {
                val data = bytes ?: return null
                if (bytes.isNotEmpty() && bytes[0] == 0x00.toByte()) {
                    val buffer = ByteBuffer.wrap(data)
                    buffer.get()
                    return buffer.getLong()
                }
                return x.deserialize(data)
            }
        }
        val template = RedisTemplate<String, Any?>()
        template.connectionFactory = redisConnectionFactory
        // 设置值什么来着
        // GenericJackson2JsonRedisSerializer
        template.keySerializer = x
        template.hashKeySerializer = x

        template.valueSerializer = valueSerializer
        template.hashValueSerializer = valueSerializer
        log.debug("init debounceRedisTemplate via: {}", valueSerializer)
        return template
    }


}