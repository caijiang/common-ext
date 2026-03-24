package io.github.caijiang.common.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory

/**
 * 日志工具类
 * @since 2.7.0
 * @author CJ
 */
object LoggingUtils {

    /**
     * 根据环境变量规则`LOG_LEVEL_{loggerName}`内容为`日志等级`，调整所有 slf4j logger 日志等级
     */
    @JvmStatic
    fun slf4jLoggerLevelFromEnvironment() {
        try {
            Class.forName("ch.qos.logback.classic.LoggerContext")
        } catch (e: Exception) {
            System.err.println("[slf4jLoggerLevelFromEnvironment]can not load class:ch.qos.logback.classic.LoggerContext")
            return
        }
        val factory = LoggerFactory.getILoggerFactory()
        if (factory is LoggerContext) {
            System.getenv().filterKeys {
                it.startsWith("LOG_LEVEL_")
            }.forEach { (t, u) ->
                val name = t.removePrefix("LOG_LEVEL_")
                val logger = factory.getLogger(name)
                logger.level = Level.toLevel(u)
            }
        } else {
            System.err.println("[slf4jLoggerLevelFromEnvironment]slf4jLogger is not logback logger")
        }
    }

}