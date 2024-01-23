package io.github.caijiang.common.hibernate


import org.redisson.api.RedissonClient
import org.redisson.hibernate.RedissonRegionFactory
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Component

/**
 * 在默认情况`hibernate`使用的二级缓存是使用本地解决方案，大部分情况下我们都需要跨jvm的解决方案。`redis`就是我们常见的选择。
 * 配置`redisson`发布的 [RedissonRegionFactory] 可以轻松使用`redis`, 但不可避免得需要配置额外的redisson标准的 redis 链接。
 *
 * 如果使用`spring boot`配置的`redis`链接，那么强烈推荐使用 [SpringRedissonRegionFactory]代替，前提是将它配置在 spring bean中。
 * ```properties
 * spring.jpa.properties.hibernate.cache.region.factory_class=io.github.caijiang.common.hibernate.SpringRedissonRegionFactory
 * ```
 * @author CJ
 */
@Component
abstract class AbstractSpringRedissonRegionFactory : RedissonRegionFactory(), BeanFactoryPostProcessor {

    companion object {
        private var factory: ConfigurableListableBeanFactory? = null
    }

    protected fun createRedissonClientFromSpring(): RedissonClient {
        if (factory == null) {
            throw IllegalStateException("please make sure put ${javaClass.name} into spring beans.")
        }
        val bean = try {
            factory!!.getBean<RedissonClient>()
        } catch (e: Exception) {
            throw IllegalStateException(
                "can not find RedissonClient, https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter ",
                e
            )
        }

        return bean
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        factory = beanFactory
    }
}