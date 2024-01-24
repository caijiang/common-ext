package io.github.caijiang.common.test.solitary

import java.lang.reflect.Method
import java.util.*

/**
 * @author CJ
 */
class RedisServerEntry(
    private val target: Any
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
}