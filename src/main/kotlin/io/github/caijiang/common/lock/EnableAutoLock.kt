package io.github.caijiang.common.lock

import org.springframework.context.annotation.Import

/**
 * @author CJ
 */
@Import(AutoLockConfig::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class EnableAutoLock
