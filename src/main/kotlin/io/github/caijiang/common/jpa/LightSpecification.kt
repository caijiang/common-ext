package io.github.caijiang.common.jpa

import io.github.caijiang.common.*
import org.springframework.data.jpa.domain.Specification
import kotlin.reflect.KProperty1

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
     * @return 用当前规格限定所在类的规格
     * @see [joinSingle]
     */
    fun <OTHER> specificationSingleRelation(
        prop: KProperty1<OTHER, T?>,
        type: JoinType = JoinType.INNER
    ): LightSpecification<OTHER> {
        return cast {
            it.joinSingle(prop, type)
        }
    }

    /**
     * @return 用当前规格限定所在类的规格
     * @see [joinSet]
     */
    fun <OTHER> specificationSetRelation(
        prop: KProperty1<OTHER, Set<T>?>,
        type: JoinType = JoinType.INNER
    ): LightSpecification<OTHER> {
        return cast {
            it.joinSet(prop, type)
        }
    }

    /**
     * @return 用当前规格限定所在类的规格
     * @see [joinCollection]
     */
    fun <OTHER> specificationCollectionRelation(
        prop: KProperty1<OTHER, Collection<T>?>,
        type: JoinType = JoinType.INNER
    ): LightSpecification<OTHER> {
        return cast {
            it.joinCollection(prop, type)
        }
    }

    /**
     * @return 用当前规格限定所在类的规格
     * @see [joinList]
     */
    fun <OTHER> specificationListRelation(
        prop: KProperty1<OTHER, List<T>?>,
        type: JoinType = JoinType.INNER
    ): LightSpecification<OTHER> {
        return cast {
            it.joinList(prop, type)
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