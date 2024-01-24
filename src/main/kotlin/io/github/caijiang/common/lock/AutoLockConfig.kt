package io.github.caijiang.common.lock

import io.github.caijiang.common.PostConstruct
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.Ordered
import org.springframework.integration.support.locks.LockRegistry
import org.springframework.util.StringUtils
import java.util.*
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author CJ
 */
@Configuration
@EnableAspectJAutoProxy
@Aspect
open class AutoLockConfig(applicationContext: ApplicationContext) : Ordered {

    private val log = LoggerFactory.getLogger(AutoLockConfig::class.java)

    private val lockRegistry: LockRegistry = try {
        applicationContext.getBean<LockRegistry>()
    } catch (e: Exception) {
        throw IllegalStateException(
            "consider add spring-boot-starter-data-redis or spring-integration-redis into your project to create LockRegistry bean",
            e
        )
    }

    private val lockerNamePrefix = "AO"
    private val evaluator = Evaluator(applicationContext)

    @PostConstruct
    fun init() {
        // 这段代码目的是为了提前暴露问题。
        try {
            val key = lockRegistry.obtain("$lockerNamePrefix-HC")
            log.info("current lock implements is " + key.javaClass.simpleName)
            key.lock()
            key.unlock()
        } catch (ignored: Throwable) {
        }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 5

    @Pointcut("@annotation(AutoLock)||@annotation(AutoLocks)")
    fun forAutoLock() {
    }

    @Around("forAutoLock()")
    fun aroundAutoLock(pjp: ProceedingJoinPoint): Any? {
        val lockers = toLockerStrings(pjp)
        if (log.isDebugEnabled) log.debug(
            "prepare into LOCK method:" + pjp.toShortString() + " with lockers:" + java.lang.String.join(
                ",",
                lockers
            )
        )
        return multiLock(lockers.toSet().toList(), pjp)
    }

    private fun multiLock(inputLockers: List<String>, pjp: ProceedingJoinPoint): Any? {
        val currentLockers = inputLockers.toMutableList()
        val lockStr = currentLockers.removeFirst().intern()

        val locker = lockRegistry.obtain(lockStr)
        log.debug(Thread.currentThread().name + " entering LOCK method:" + pjp.toShortString() + " with locker instance: " + lockStr)
        locker.lock()
        try {
            return if (currentLockers.isEmpty()) {
                pjp.proceed()
            } else {
                multiLock(currentLockers, pjp)
            }
        } finally {
            locker.unlock()
            log.debug(Thread.currentThread().name + " exited LOCK method:" + pjp.toShortString())
        }
    }

    private fun toLockerStrings(point: ProceedingJoinPoint): List<String> {
        // this 就是 spring bean
        // target 就是实际的对象
        val method = (point.signature as? MethodSignature)
            ?: throw IllegalStateException("can not work for signature:${point.signature}")
        val list = method.method.kotlinFunction?.findAnnotations(AutoLock::class)
        if (list.isNullOrEmpty()) {
            throw IllegalStateException("can not work for signature:${point.signature}; AutoLock not be found.")
        }
        return list.map {
            val keyName = if (StringUtils.hasText(it.value)) it.value else point.toLongString()
            val key = if (StringUtils.hasText(it.key)) {
                val context = evaluator.createEvaluationContext(method.method, point.args, point.target)
                val expression = evaluator.getCachedExpression(it.key, it)
                Objects.toString(expression.getValue(context))
            } else Objects.toString(point.args.getOrNull(0))
            "$lockerNamePrefix-$keyName-$key"
        }
    }


}