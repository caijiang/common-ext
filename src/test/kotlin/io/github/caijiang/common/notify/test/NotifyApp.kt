package io.github.caijiang.common.notify.test

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

/**
 * @author CJ
 */
@SpringBootApplication(
    exclude = [RepositoryRestMvcAutoConfiguration::class, DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
//        WebMvcAutoConfiguration::class, DispatcherServletAutoConfiguration::class, ErrorMvcAutoConfiguration::class,
        JpaRepositoriesAutoConfiguration::class]
)
open class NotifyApp