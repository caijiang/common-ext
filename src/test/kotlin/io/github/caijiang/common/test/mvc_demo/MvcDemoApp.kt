package io.github.caijiang.common.test.mvc_demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

/**
 * @author CJ
 */
@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, JpaRepositoriesAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
@EnableWebMvc
open class MvcDemoApp