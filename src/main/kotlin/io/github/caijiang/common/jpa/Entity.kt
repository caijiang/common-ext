package io.github.caijiang.common.jpa

import org.springframework.util.ClassUtils


/**
 * @since 0.0.4
 * @return 返回一个运行时 Jpa Entity 的真实 Class
 */
fun Any.jpaEntityEffectiveClass(): Class<*> {
    return if (ClassUtils.isPresent("org.hibernate.proxy.HibernateProxy", null)) {
        if (this is org.hibernate.proxy.HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
    } else {
        this.javaClass
    }
}

fun Any.jpaEntityHashCode(): Int {
    return if (ClassUtils.isPresent("org.hibernate.proxy.HibernateProxy", null)) {
        if (this is org.hibernate.proxy.HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else this.javaClass.hashCode()
    } else {
        this.javaClass.hashCode()
    }
}