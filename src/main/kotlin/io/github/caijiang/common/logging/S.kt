package io.github.caijiang.common.logging

import org.springframework.boot.logging.LogLevel

/**
 * @since 2.1.0
 * @author CJ
 */
fun org.slf4j.Logger.toLoggingApi(): LoggingApi {
    val self = this
    return object : LoggingApi {
        override fun logMessage(level: LogLevel, message: String, throwable: Throwable?) {
            when (level) {
                LogLevel.TRACE -> self.trace(message, throwable)
                LogLevel.DEBUG -> self.debug(message, throwable)
                LogLevel.INFO -> self.info(message, throwable)
                LogLevel.WARN -> self.warn(message, throwable)
                LogLevel.ERROR -> self.error(message, throwable)
                LogLevel.FATAL -> self.error(message, throwable)
                LogLevel.OFF -> {

                }
            }
        }
    }
}