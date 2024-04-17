package io.github.caijiang.common.jpa


/**
 * @since 0.0.4
 * @return 返回一个运行时 Jpa Entity 的真实 Class
 */
fun Any.jpaEntityEffectiveClass(): Class<*> {
    return JpaUtils.jpaEntityEffectiveClass(this)
}

fun Any.jpaEntityHashCode(): Int {
    return JpaUtils.jpaEntityHashCode(this)
}