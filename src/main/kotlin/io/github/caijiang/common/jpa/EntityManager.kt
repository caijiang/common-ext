package io.github.caijiang.common.jpa

import javax.persistence.EntityManager
import javax.persistence.Tuple
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

fun <T> EntityManager.createCriteriaQuery(type: Class<T>, spec: LightSpecification<T>? = null): TypedQuery<T> {
    return JpaUtils.createCriteriaQuery(this, type, spec)
}

fun <T> EntityManager.createCriteriaQueryWithTuple(
    type: Class<T>, tuple: CriteriaQuery<Tuple>.(
        Root<T>, CriteriaBuilder
    ) -> CriteriaQuery<Tuple>, spec: LightSpecification<T>? = null
): TypedQuery<Tuple> {
    return JpaUtils.createCriteriaQueryWithTuple(this, type, tuple, spec)
}