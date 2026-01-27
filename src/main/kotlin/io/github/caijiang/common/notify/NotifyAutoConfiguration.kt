package io.github.caijiang.common.notify

import io.github.caijiang.common.notify.config.NotifyConfiguration
import io.github.caijiang.common.notify.config.SendNoticeServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * @author CJ
 */
@AutoConfiguration
@EnableConfigurationProperties(NotifyConfiguration::class)
open class NotifyAutoConfiguration {

    @Bean
    open fun sendNoticeService(notifyConfiguration: NotifyConfiguration): SendNoticeService =
        SendNoticeServiceImpl(notifyConfiguration)

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(name = ["org.springframework.web.servlet.DispatcherServlet"])
    open fun notifyDebugController(sendNoticeService: SendNoticeService) = DebugController(sendNoticeService)
}