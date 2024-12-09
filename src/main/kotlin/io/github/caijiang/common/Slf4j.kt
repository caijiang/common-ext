package io.github.caijiang.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @since 0.1.0
 * @author CJ
 */
@Suppress("UnusedReceiverParameter")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slf4j {
    companion object {
        val <reified T> T.log: Logger
            inline get() = LoggerFactory.getLogger(T::class.java)
    }
}

