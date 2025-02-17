package io.github.caijiang.common.logging

import org.springframework.boot.logging.LogLevel

/**
 * @author CJ
 * @since 2.1.0
 */
@FunctionalInterface
interface LoggingApi {
    fun logMessage(level: LogLevel, message: String, throwable: Throwable? = null)
}