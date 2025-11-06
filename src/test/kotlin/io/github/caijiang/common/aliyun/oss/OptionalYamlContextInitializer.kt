package io.github.caijiang.common.aliyun.oss

import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.io.ClassPathResource


/**
 * @author CJ
 */
class OptionalYamlContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        val resource = ClassPathResource("/local-aliyun-oss.yaml")
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        if (resource.exists()) {
            val loader = YamlPropertySourceLoader()
            loader.load(resource.filename!!, resource).forEach {
                applicationContext.environment.propertySources.addLast(it)
            }
        }
    }
}