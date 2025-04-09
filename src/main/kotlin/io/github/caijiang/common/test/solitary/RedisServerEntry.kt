package io.github.caijiang.common.test.solitary

import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import java.lang.reflect.Method
import java.util.*

/**
 * @author CJ
 */
class RedisServerEntry(
    private val target: Any,
    val port: Int,
    val password: String
) {

    fun start() {
        val method = Arrays.stream(target.javaClass.methods)
            .filter { it: Method -> it.name == "start" }
            .findAny()
            .orElseThrow {
                IllegalStateException(
                    "找不到 start"
                )
            }

        method.invoke(target)
    }

    fun stop() {
        val method = Arrays.stream(target.javaClass.methods)
            .filter { it: Method -> it.name == "stop" }
            .findAny()
            .orElseThrow {
                IllegalStateException(
                    "找不到 stop"
                )
            }

        method.invoke(target)
    }

    fun toRedisStandaloneConfiguration(): RedisStandaloneConfiguration {
        val conf = RedisStandaloneConfiguration("localhost", port)
        conf.setPassword(password)

        return conf
    }
}