package io.github.caijiang.common.lock


/**
 * @author CJ
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@JvmRepeatable(AutoLocks::class)
annotation class AutoLock(
    /**
     * 锁名称，缺省就是方法签名。
     */
    val value: String = "",
    /**
     * Spring EL，缺省则取第一个参数的 toString (第一个参数是 null 则取 `null`)
     */
    val key: String = "",
)