package io.github.caijiang.common.debounce

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.util.UtilUtils
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.AllNestedConditions
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.*
import org.springframework.core.type.AnnotatedTypeMetadata
import java.time.Duration

/**
 * 必备配置
 */
internal class TestCondition : AllNestedConditions(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN) {

    class C0 : Condition {
        override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
            return UtilUtils.runInTest()
        }
    }

    @ConditionalOnBean(DebounceCallbackService::class)
    class C2

}

/**
 * 自动注册测试用的[DebounceService],它的条件非常简单:
 * 1. 在单元测试运行
 * 1. 声明了[DebounceCallbackService]
 * 1. 没有明式声明`noMockDebounce` profile
 * @author CJ
 */
@AutoConfiguration
@Profile("!noMockDebounce")
@Conditional(TestCondition.C0::class, TestCondition::class)
@AutoConfigureBefore(DebounceRocketMQAutoConfiguration::class)
open class DebounceTestAutoConfiguration(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val callbackService: DebounceCallbackService
) {

    @Bean
    open fun mockDebounceService(): DebounceService {
        return object : DebounceService {
            override fun debounce(type: String, arg: String, debounceDuration: Duration, deathDuration: Duration) {
                log.info("这里是单元测试，所以直接执行防抖回调:{}:{}", type, arg)
                callbackService.invokeBusiness(type, arg)
            }
        }
    }

}