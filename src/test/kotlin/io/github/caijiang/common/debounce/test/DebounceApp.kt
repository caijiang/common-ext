package io.github.caijiang.common.debounce.test

import io.github.caijiang.common.PostConstruct
import io.github.caijiang.common.PreDestroy
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.debounce.DebounceCallbackService
import io.github.caijiang.common.debounce.DelayMQData
import io.github.caijiang.common.debounce.MqSender
import io.github.caijiang.common.test.solitary.RedisServerEntry
import io.github.caijiang.common.test.solitary.SolitaryHelper
import org.apache.rocketmq.spring.core.RocketMQTemplate
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.messaging.Message
import java.io.Serializable
import java.time.Duration
import java.util.*

/**
 * @author CJ
 */
@SpringBootApplication(
    exclude = [RepositoryRestMvcAutoConfiguration::class, DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        JpaRepositoriesAutoConfiguration::class]
)
open class DebounceApp : MqSender, DebounceCallbackService {

    private lateinit var redisInstance: RedisServerEntry
    val sendingMqMessages: MutableList<Pair<Message<DelayMQData>, Duration>> =
        Collections.synchronizedList(mutableListOf())
    val invokingBusinessList: MutableList<Pair<String, Serializable>> = Collections.synchronizedList(mutableListOf())

    @PostConstruct
    fun start() {
        log.info("Starting application")
//        redisInstance = SolitaryHelper.createRedisWithPort("myPassword", 19191)
        redisInstance = SolitaryHelper.createRedis(null)
        log.info("local redis: {}:{}", redisInstance.port, redisInstance.password)
    }

    @PreDestroy
    fun closed() {
        log.info("Closed application")
        redisInstance.stop()
    }

    @Bean
    open fun rocketMQTemplate(): RocketMQTemplate {
        return Mockito.mock(RocketMQTemplate::class.java)
    }

    @Bean
    open fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(redisInstance.toRedisStandaloneConfiguration())
    }
//
//    @Bean
//    open fun debounceCallbackService(): DebounceCallbackService {
//        return Mockito.mock(DebounceCallbackService::class.java)
//    }

    override fun sendDelay(message: Message<DelayMQData>, duration: Duration) {
        sendingMqMessages.add(Pair(message, duration))
    }

    override fun invokeBusiness(type: String, arg: String) {
        invokingBusinessList.add(Pair(type, arg))
    }
}