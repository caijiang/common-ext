package io.github.caijiang.common.debounce

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.caijiang.common.debounce.config.DebounceProperties
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration
import org.apache.rocketmq.spring.core.RocketMQTemplate
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.AllNestedConditions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

/**
 * rocketMQ 约定配置
 */
class RocketMQRequiredCondition : AllNestedConditions(ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION) {
    @ConditionalOnProperty(prefix = "common.debounce", name = ["topic"], matchIfMissing = false)
    class Topic1

    @ConditionalOnProperty(prefix = "common.debounce", name = ["rocket-mq-consumer-group"], matchIfMissing = false)
    class Topic2
}

/**
 * 必备配置
 */
class RocketRequiredCondition : AllNestedConditions(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN) {
    @ConditionalOnClass(
        KotlinModule::class,
        RedisTemplate::class,
        RedisConnectionFactory::class,
        RocketMQTemplate::class,
        DebounceCallbackService::class
    )
    class C1

    @ConditionalOnBean(RedisConnectionFactory::class, RocketMQTemplate::class, DebounceCallbackService::class)
    class C2
}

/**
 * @author CJ
 */
@AutoConfiguration
@Conditional(RocketMQRequiredCondition::class, RocketRequiredCondition::class)
@EnableConfigurationProperties(DebounceProperties::class)
@Import(DebounceConfiguration::class)
@AutoConfigureAfter(RocketMQAutoConfiguration::class, RedisAutoConfiguration::class)
open class DebounceRocketMQAutoConfiguration {
    /**
     * 真正载入 rocketmq 方面的 bean
     */
    @Profile("!skipRocketMq")
    @Bean
    open fun goingToLoadAllRocketMq(): BeanDefinitionRegistryPostProcessor {
        return object : BeanDefinitionRegistryPostProcessor {
            override fun postProcessBeanFactory(p0: ConfigurableListableBeanFactory) {
            }

            override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
                val scanner = ClassPathBeanDefinitionScanner(registry)
                scanner.scan("io.github.caijiang.common.debounce.rocket")
            }
        }
    }

    @Bean
    open fun goingToLoadAllRocketMq2(): BeanDefinitionRegistryPostProcessor {
        return object : BeanDefinitionRegistryPostProcessor {
            override fun postProcessBeanFactory(p0: ConfigurableListableBeanFactory) {
            }

            override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
                val scanner = ClassPathBeanDefinitionScanner(registry)
                scanner.scan("io.github.caijiang.common.debounce.bean")
            }
        }
    }
}