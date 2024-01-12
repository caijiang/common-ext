package io.github.caijiang.common.jpa

import io.github.caijiang.common.CriteriaBuilder
import io.github.caijiang.common.CriteriaQuery
import io.github.caijiang.common.From
import io.github.caijiang.common.Predicate
import org.springframework.data.jpa.domain.Specification

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

    fun <X : T> upcast(): LightSpecification<X> {
        return LightSpecification { root, query, cb ->
            toPredicate(root, query, cb)
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

    operator fun not(): LightSpecification<T> = not(this)

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