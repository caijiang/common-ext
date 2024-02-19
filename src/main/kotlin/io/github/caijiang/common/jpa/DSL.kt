package io.github.caijiang.common.jpa

import io.github.caijiang.common.*
import kotlin.reflect.KProperty1

//https://github.com/consoleau/kotlin-jpa-LightSpecification-dsl

//<editor-fold desc="Helper to allow joining to Properties">
/**
 */
fun <Z, T, R> From<Z, out T>.joinSingle(prop: KProperty1<T, R?>, type: JoinType = JoinType.INNER): Join<T, R> =
    this.join(prop.name, type)

/**
 */
fun <Z, T, R> From<Z, out T>.joinSet(prop: KProperty1<T, Set<R>?>, type: JoinType = JoinType.INNER): SetJoin<T, R> =
    this.joinSet(prop.name, type)

/**
 */
fun <Z, T, R> From<Z, out T>.joinCollection(
    prop: KProperty1<T, Collection<R>?>,
    type: JoinType = JoinType.INNER
): CollectionJoin<T, R> =
    this.joinCollection(prop.name, type)

/**
 */
fun <Z, T, R> From<Z, out T>.joinList(
    prop: KProperty1<T, List<R>?>,
    type: JoinType = JoinType.INNER
): ListJoin<T, R> =
    this.joinList(prop.name, type)

/**
 */
fun <Z, T, R, V> From<Z, out T>.joinMap(
    prop: KProperty1<T, Map<R, V>?>,
    type: JoinType = JoinType.INNER
): MapJoin<T, R, V> =
    this.joinMap(prop.name, type)
//</editor-fold>

// Helper to enable get by Property
fun <R> Path<*>.get(prop: KProperty1<*, R?>): Path<R> = this.get(prop.name)

// Version of LightSpecification.where that makes the CriteriaBuilder implicit
fun <T> where(makePredicate: CriteriaBuilder.(From<*, out T>) -> Predicate?): LightSpecification<T> =
    LightSpecification { root, _, criteriaBuilder -> criteriaBuilder.makePredicate(root) }

// helper function for defining LightSpecification that take a Path to a property and send it to a CriteriaBuilder
private fun <T, R> KProperty1<T, R?>.spec(makePredicate: CriteriaBuilder.(path: Path<R>) -> Predicate?): LightSpecification<T> =
    let { property -> where { root -> makePredicate(root.get(property)) } }

// Equality
fun <T, R> KProperty1<T, R?>.equal(x: R): LightSpecification<T> = spec { equal(it, x) }

fun <T, R> KProperty1<T, R?>.notEqual(x: R): LightSpecification<T> = spec { notEqual(it, x) }

/**
 * Ignores empty collection otherwise an empty 'in' predicate will be generated which will never match any results
 */
fun <T, R : Any> KProperty1<T, R?>.`in`(values: Collection<R>): LightSpecification<T> =
    if (values.isNotEmpty()) spec { path ->
        `in`(path).apply { values.forEach { this.value(it) } }
    } else spec {
        disjunction()
    }

// Comparison
fun <T> KProperty1<T, Number?>.le(x: Number) = spec { le(it, x) }

fun <T> KProperty1<T, Number?>.lt(x: Number) = spec { lt(it, x) }
fun <T> KProperty1<T, Number?>.ge(x: Number) = spec { ge(it, x) }
fun <T> KProperty1<T, Number?>.gt(x: Number) = spec { gt(it, x) }
fun <T, R : Comparable<R>> KProperty1<T, R?>.lessThan(x: R) = spec { lessThan(it, x) }
fun <T, R : Comparable<R>> KProperty1<T, R?>.lessThanOrEqualTo(x: R) = spec { lessThanOrEqualTo(it, x) }
fun <T, R : Comparable<R>> KProperty1<T, R?>.greaterThan(x: R) = spec { greaterThan(it, x) }
fun <T, R : Comparable<R>> KProperty1<T, R?>.greaterThanOrEqualTo(x: R) = spec { greaterThanOrEqualTo(it, x) }
fun <T, R : Comparable<R>> KProperty1<T, R?>.between(x: R, y: R) = spec { between(it, x, y) }

// True/False
fun <T> KProperty1<T, Boolean?>.isTrue() = spec { isTrue(it) }

fun <T> KProperty1<T, Boolean?>.isFalse() = spec { isFalse(it) }

// Null / NotNull
fun <T, R> KProperty1<T, R?>.isNull() = spec { isNull(it) }

fun <T, R> KProperty1<T, R?>.isNotNull() = spec { isNotNull(it) }

// Collections
fun <T, R : Collection<*>> KProperty1<T, R?>.isEmpty() = spec { isEmpty(it) }

fun <T, R : Collection<*>> KProperty1<T, R?>.isNotEmpty() = spec { isNotEmpty(it) }
fun <T, E, R : Collection<E>> KProperty1<T, R?>.isMember(elem: E) = spec { isMember(elem, it) }
fun <T, E, R : Collection<E>> KProperty1<T, R?>.isNotMember(elem: E) = spec { isNotMember(elem, it) }

// Strings
fun <T> KProperty1<T, String?>.like(x: String): LightSpecification<T> = spec { like(it, x) }

fun <T> KProperty1<T, String?>.like(x: String, escapeChar: Char): LightSpecification<T> =
    spec { like(it, x, escapeChar) }

fun <T> KProperty1<T, String?>.notLike(x: String): LightSpecification<T> = spec { notLike(it, x) }
fun <T> KProperty1<T, String?>.notLike(x: String, escapeChar: Char): LightSpecification<T> =
    spec { notLike(it, x, escapeChar) }

// And
//infix fun <T> LightSpecification<T>.and(other: LightSpecification<T>): LightSpecification<T> = this.and(other)
//
//inline fun <reified T> and(vararg specs: LightSpecification<T>?): LightSpecification<T> {
//    return and(specs.toList())
//}
//
//inline fun <reified T> and(specs: Iterable<LightSpecification<T>?>): LightSpecification<T> {
//    return combineSpecification(specs, LightSpecification<T>::and)
//}

// Or
//infix fun <T> LightSpecification<T>.or(other: LightSpecification<T>): LightSpecification<T> = this.or(other)
//
//inline fun <reified T> or(vararg specs: LightSpecification<T>?): LightSpecification<T> {
//    return or(specs.toList())
//}
//
//inline fun <reified T> or(specs: Iterable<LightSpecification<T>?>): LightSpecification<T> {
//    return combineSpecification(specs, LightSpecification<T>::or)
//}

// Not
//operator fun <T> LightSpecification<T>.not(): LightSpecification<T> = LightSpecification.not(this)

// Combines LightSpecification with an operation
//inline fun <reified T> combineSpecification(
//    specs: Iterable<LightSpecification<T>?>,
//    operation: LightSpecification<T>.(LightSpecification<T>) -> LightSpecification<T>
//): LightSpecification<T> {
//    return specs.filterNotNull().fold(emptySpecification()) { existing, new -> existing.operation(new) }
//}

// Empty LightSpecification
//inline fun <reified T> emptySpecification(): LightSpecification<T> = LightSpecification.where(null)