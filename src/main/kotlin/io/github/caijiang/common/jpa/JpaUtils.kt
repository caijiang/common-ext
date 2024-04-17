package io.github.caijiang.common.jpa

import io.github.caijiang.common.*
import org.springframework.util.ClassUtils


/**
 * 同时面向 java 和 kotlin
 * @author CJ
 */
object JpaUtils {

    @JvmStatic
    fun jpaEntityEffectiveClass(entity: Any): Class<*> {
        return if (ClassUtils.isPresent("org.hibernate.proxy.HibernateProxy", null)) {
            if (entity is org.hibernate.proxy.HibernateProxy) entity.hibernateLazyInitializer.persistentClass else entity.javaClass
        } else {
            entity.javaClass
        }
    }

    @JvmStatic
    fun jpaEntityHashCode(entity: Any): Int {
        return if (ClassUtils.isPresent("org.hibernate.proxy.HibernateProxy", null)) {
            if (entity is org.hibernate.proxy.HibernateProxy) entity.hibernateLazyInitializer.persistentClass.hashCode() else entity.javaClass.hashCode()
        } else {
            entity.javaClass.hashCode()
        }
    }


    @JvmStatic
    fun <T> createCriteriaQuery(
        entityManager: EntityManager,
        type: Class<T>,
        spec: LightSpecification<T>?
    ): TypedQuery<T> {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(type)
        val root = cq.from(type)
        val predicates = listOfNotNull(spec?.toPredicate(root, cq, cb))
            .toTypedArray()
        return entityManager.createQuery(
            cq
                .select(root)
                .where(*predicates)
        )
    }

    @JvmStatic
    fun <T> createCriteriaQueryWithTuple(
        entityManager: EntityManager,
        type: Class<T>,
        tuple: CriteriaQuery<Tuple>.(Root<T>, CriteriaBuilder) -> CriteriaQuery<Tuple>,
        spec: LightSpecification<T>?
    ): TypedQuery<Tuple> {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createTupleQuery()
        val root = cq.from(type)
        val predicates = listOfNotNull(spec?.toPredicate(root, cq, cb))
            .toTypedArray()
        return entityManager.createQuery(
            tuple(cq, root, cb)
                .where(*predicates)
        )
    }

    @JvmStatic
    fun removeAllCache(entityManager: EntityManager) {
        val cache = entityManager.entityManagerFactory.cache
        cache.evictAll()
        if (ClassUtils.isPresent("org.hibernate.Cache", null))
            (cache as? org.hibernate.Cache)?.evictAllRegions()
    }

}