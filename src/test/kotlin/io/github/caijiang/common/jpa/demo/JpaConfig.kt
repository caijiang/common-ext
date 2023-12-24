package io.github.caijiang.common.jpa.demo

import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.util.ClassUtils
import javax.sql.DataSource

/**
 * @author CJ
 */
@Configuration
@EnableJpaRepositories(basePackages = ["io.github.caijiang.common.jpa.demo.repository"])
open class JpaConfig {

    private val hibernateActive = ClassUtils.isPresent("org.hibernate.Hibernate", null)

    @Bean
    open fun dataSource(): DataSource {
        val bean = DriverManagerDataSource()
        bean.setDriverClassName("org.h2.Driver")
        bean.url = "jdbc:h2:mem:db;DB_CLOSE_DELAY=-1;MODE=MYSQL;"
        return bean
    }

    @Bean
    open fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val bean = LocalContainerEntityManagerFactoryBean()
        if (hibernateActive) {
            bean.jpaVendorAdapter = HibernateJpaVendorAdapter()
                .apply {
                    setShowSql(true)
                    setGenerateDdl(true)
                }
//        bean.persistenceUnitName = "default"
            bean.jpaPropertyMap.putAll(
                mapOf(
                    "hibernate.transaction.jta.platform" to NoJtaPlatform.INSTANCE,
                    "hibernate.hbm2ddl.auto" to "create",
                    "hibernate.id.new_generator_mappings" to "true",
//                "hibernate.physical_naming_strategy" to "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
//                "hibernate.resource.beans.container" to beanFactory,
//                "hibernate.implicit_naming_strategy" to "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy",
                    "hibernate.archive.scanner" to "org.hibernate.boot.archive.scan.internal.DisabledScanner"
                )
            )
        } else {
            bean.jpaVendorAdapter = EclipseLinkJpaVendorAdapter()
                .apply {
                    setShowSql(true)
                    setGenerateDdl(true)
                }
            bean.jpaPropertyMap.putAll(
                mutableMapOf(
                    "eclipselink.weaving" to "false"
                )
            )
        }

        bean.dataSource = dataSource()
        bean.setPackagesToScan("io.github.caijiang.common.jpa.demo.entity")
        return bean
    }

    @Bean
    open fun transactionManager(
    ): PlatformTransactionManager {
        return JpaTransactionManager()
    }

}