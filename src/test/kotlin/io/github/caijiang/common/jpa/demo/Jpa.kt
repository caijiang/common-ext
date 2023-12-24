package io.github.caijiang.common.jpa.demo

import org.springframework.util.ClassUtils


fun Any.jpaEntityEffectiveClass(): Class<*> {
    return if (ClassUtils.isPresent("org.hibernate.proxy.HibernateProxy", null)) {
        if (this is org.hibernate.proxy.HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
    } else {
        this.javaClass
    }
}

fun Any.jpaHashCode(): Int {
    return if (ClassUtils.isPresent("org.hibernate.proxy.HibernateProxy", null)) {
        if (this is org.hibernate.proxy.HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else this.javaClass.hashCode()
    } else {
        this.javaClass.hashCode()
    }
}