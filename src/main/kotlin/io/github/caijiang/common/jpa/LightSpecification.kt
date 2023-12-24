package io.github.caijiang.common.jpa

import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.From
import javax.persistence.criteria.Predicate

/**
 *
 * 轻量级规格，可以通过[toSpring]转换为 spring 规格
 * @author CJ
 */
@FunctionalInterface
fun interface LightSpecification<T> {

    companion object {
        /**
         * @see Specification.not
         */
        @JvmStatic
        fun <T> not(spec: LightSpecification<T>?): LightSpecification<T> {
            return LightSpecifications.negated(spec)
        }

        /**
         * @see Specification.where
         */
        @JvmStatic
        fun <T> where(spec: LightSpecification<T>?): LightSpecification<T> {
            return spec
                ?: LightSpecification { _: From<*, out T?>?, _: CriteriaQuery<*>?, _: CriteriaBuilder? -> null }
        }
    }


    /**
     * @see Specification.and
     */
    fun and(other: LightSpecification<T>?): LightSpecification<T> {
        return LightSpecifications.composed(this, other, LightSpecifications.CompositionType.AND)
    }

    /**
     * @see Specification.or
     */
    fun or(other: LightSpecification<T>?): LightSpecification<T> {
        return LightSpecifications.composed(this, other, LightSpecifications.CompositionType.OR)
    }

    /**
     * 转换成其他实体的规格
     * @param work 转换器
     */
    fun <OTHER> cast(work: CriteriaBuilder.(From<*, out OTHER>) -> From<*, T>): LightSpecification<OTHER> {
        return LightSpecification { root, query, cb ->
            toPredicate(work(cb, root), query, cb)
        }
    }

    /**
     * @see org.springframework.data.jpa.domain.Specification.toPredicate
     */
    fun toPredicate(root: From<*, out T>, query: CriteriaQuery<*>?, cb: CriteriaBuilder): Predicate?

    /**
     * @return spring-data 的规格
     */
    fun toSpring(): Specification<T> {
        return Specification { root, query, criteriaBuilder ->
            toPredicate(root, query, criteriaBuilder)
        }
    }
}