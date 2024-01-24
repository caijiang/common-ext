package io.github.caijiang.common.lock

/**
 * @author CJ
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AutoLocks(val value: Array<AutoLock>)