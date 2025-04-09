package io.github.caijiang.common.debounce

import io.github.caijiang.common.debounce.config.DebounceProperties
import org.apache.rocketmq.spring.core.RocketMQTemplate
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.AllNestedConditions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.data.redis.connection.RedisConnectionFactory

class RocketMQRequiredCondition : AllNestedConditions(ConfigurationCondition.ConfigurationPhase.PARSE_CONFIGURATION) {
    @ConditionalOnProperty(prefix = "common.debounce", name = ["topic"], matchIfMissing = false)
    class Topic1

    @ConditionalOnProperty(prefix = "common.debounce", name = ["rocket-mq-consumer-group"], matchIfMissing = false)
    class Topic2
}

/**
 * @author CJ
 */
@AutoConfiguration
@ConditionalOnBean(RedisConnectionFactory::class, RocketMQTemplate::class, DebounceCallbackService::class)
@Conditional(RocketMQRequiredCondition::class)
@EnableConfigurationProperties(DebounceProperties::class)
@Import(DebounceConfiguration::class)
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
}