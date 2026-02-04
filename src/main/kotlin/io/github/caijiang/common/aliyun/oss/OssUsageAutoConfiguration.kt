package io.github.caijiang.common.aliyun.oss

import com.aliyun.oss.OSSClientBuilder
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * 明式关闭就关闭，暗示配置则启动
 * @author CJ
 */
@AutoConfiguration
@ConditionalOnClass(OSSClientBuilder::class)
@ConditionalOnMissingBean(OssUsageService::class)
@ConditionalOnProperty(prefix = "oss", name = ["pub-bucket", "prv-bucket", "access-secret", "region"])
@EnableConfigurationProperties(OssProperties::class)
open class OssUsageAutoConfiguration {
    @Bean
    open fun ossUsageService(properties: OssProperties): OssUsageService {
        return OssUsageServiceImpl(properties)
    }
}