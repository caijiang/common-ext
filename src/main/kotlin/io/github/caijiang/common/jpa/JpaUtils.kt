package io.github.caijiang.common.jpa

import io.github.caijiang.common.*


/**
 * 同时面向 java 和 kotlin
 * @author CJ
 */
object JpaUtils {

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

}