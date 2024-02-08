package io.github.caijiang.common.test.jpa_rest_demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer


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

        }
    }
}