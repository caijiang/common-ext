package io.github.caijiang.common.test.jpa_rest_demo

import io.github.caijiang.common.jpa.demo.entity.DepartmentMonthScore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver


/**
 * @author CJ
 */
@SpringBootApplication
@EnableJpaRepositories("io.github.caijiang.common.jpa.demo.repository")
@EntityScan("io.github.caijiang.common.jpa.demo.entity")
open class JpaRestDemoApp {
    @Bean
    open fun repositoryRestConfigurer(): RepositoryRestConfigurer {

        return object : RepositoryRestConfigurer {
            override fun configureRepositoryRestConfiguration(
                config: RepositoryRestConfiguration?,
                cors: CorsRegistry?
            ) {
                super.configureRepositoryRestConfiguration(config, cors)
                config?.exposeIdsFor(DepartmentMonthScore::class.java)
            }

            override fun configureExceptionHandlerExceptionResolver(exceptionResolver: ExceptionHandlerExceptionResolver?) {
                super.configureExceptionHandlerExceptionResolver(exceptionResolver)
            }
        }
    }
}