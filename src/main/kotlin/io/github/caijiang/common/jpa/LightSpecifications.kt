package io.github.caijiang.common.jpa

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate

/**
 * @author CJ
 */
internal object LightSpecifications {
    fun <T> composed(
        lhs: LightSpecification<T>?, rhs: LightSpecification<T>?, compositionType: CompositionType
    ): LightSpecification<T> {
        return LightSpecification { root, query, builder ->
            val otherPredicate =
                rhs?.toPredicate(root, query, builder)
            val thisPredicate =
                lhs?.toPredicate(root, query, builder)
            if (thisPredicate == null) otherPredicate
            else if (otherPredicate == null) thisPredicate
            else compositionType.combine(
                builder,
                thisPredicate,
                otherPredicate
            )
        }
    }

    fun <T> negated(spec: LightSpecification<T>?): LightSpecification<T> {
        return LightSpecification { root, query, builder ->
            if (spec == null) null else builder.not(
                spec.toPredicate(
                    root,
                    query,
                    builder
                )
            )
        }
    }

    internal enum class CompositionType {
        AND {

            override fun combine(builder: CriteriaBuilder, lhs: Predicate?, rhs: Predicate?): Predicate? {
                return builder.and(lhs, rhs)
            }
        },
        OR {
            override fun combine(builder: CriteriaBuilder, lhs: Predicate?, rhs: Predicate?): Predicate {
                return builder.or(lhs, rhs)
            }
        };

        abstract fun combine(builder: CriteriaBuilder, lhs: Predicate?, rhs: Predicate?): Predicate?
    }

}