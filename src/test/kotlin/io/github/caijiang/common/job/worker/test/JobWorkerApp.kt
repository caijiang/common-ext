package io.github.caijiang.common.job.worker.test

import io.github.caijiang.common.job.worker.JobTypeRunner
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.Bean

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
open class JobWorkerApp {
    @Bean
    open fun jobTypeRunner(): JobTypeRunner = mock()
}