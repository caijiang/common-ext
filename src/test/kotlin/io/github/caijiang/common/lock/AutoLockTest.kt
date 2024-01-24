package io.github.caijiang.common.lock

import io.github.caijiang.common.lock.demo.LockWorker
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.integration.support.locks.LockRegistry
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import kotlin.test.Test

/**
 * @author CJ
 */
@SpringJUnitConfig
class AutoLockTest {

    @Configuration
    @ComponentScan("io.github.caijiang.common.lock.demo")
    @EnableAutoLock
    internal open class LockDemoApp

    @Autowired
    private lateinit var lockWorker: LockWorker

    @MockBean
    private lateinit var lockRegistry: LockRegistry

    private enum class ActionType {
        Create, Lock, Unlock
    }

    private data class ActionData(
        val type: ActionType,
        val text: String,
        val time: Long = System.nanoTime()
    )


    @Test
    fun g1() {
        val actions = mutableListOf<ActionData>()

        class TestLocker(val text: String) : Lock {
            override fun lock() {
                actions.add(ActionData(ActionType.Lock, text))
            }

            override fun unlock() {
                actions.add(ActionData(ActionType.Unlock, text))
            }

            override fun lockInterruptibly() {
            }

            override fun tryLock(): Boolean = true

            override fun tryLock(time: Long, unit: TimeUnit): Boolean = true

            override fun newCondition(): Condition {
                throw IllegalStateException()
            }
        }

        Mockito.`when`(lockRegistry.obtain(Mockito.any()))
            .thenAnswer {
                val text = it.arguments[0].toString()
                actions.add(ActionData(ActionType.Create, text))
                return@thenAnswer TestLocker(text)
            }
        lockWorker.withoutParameters()


        val key1 = "AO-execution(public void io.github.caijiang.common.lock.demo.LockWorker.withoutParameters())-null"
        assertThat(actions)
            .element(0).`is`(org.assertj.core.api.Condition({
                it.text == key1 && it.type == ActionType.Create
            }, ""))
        assertThat(actions)
            .element(1).`is`(org.assertj.core.api.Condition({
                it.text == key1 && it.type == ActionType.Lock
            }, ""))
        assertThat(actions)
            .element(2).`is`(org.assertj.core.api.Condition({
                it.text == key1 && it.type == ActionType.Unlock
            }, ""))


        actions.clear()
        lockWorker.withDefaultAutoLock(null)

        val key2 =
            "AO-execution(public void io.github.caijiang.common.lock.demo.LockWorker.withDefaultAutoLock(java.lang.String))-null"
        assertThat(actions)
            .element(0).`is`(org.assertj.core.api.Condition({
                it.text == key2 && it.type == ActionType.Create
            }, ""))
        assertThat(actions)
            .element(1).`is`(org.assertj.core.api.Condition({
                it.text == key2 && it.type == ActionType.Lock
            }, ""))
        assertThat(actions)
            .element(2).`is`(org.assertj.core.api.Condition({
                it.text == key2 && it.type == ActionType.Unlock
            }, ""))


        actions.clear()
        lockWorker.withDefaultAutoLock("1")
        val key3 =
            "AO-execution(public void io.github.caijiang.common.lock.demo.LockWorker.withDefaultAutoLock(java.lang.String))-1"
        assertThat(actions)
            .element(0).`is`(org.assertj.core.api.Condition({
                it.text == key3 && it.type == ActionType.Create
            }, ""))
        assertThat(actions)
            .element(1).`is`(org.assertj.core.api.Condition({
                it.text == key3 && it.type == ActionType.Lock
            }, ""))
        assertThat(actions)
            .element(2).`is`(org.assertj.core.api.Condition({
                it.text == key3 && it.type == ActionType.Unlock
            }, ""))


        actions.clear()
        lockWorker.lockWithName()
        val key4 =
            "AO-name1-null"
        assertThat(actions)
            .element(0).`is`(org.assertj.core.api.Condition({
                it.text == key4 && it.type == ActionType.Create
            }, ""))
        assertThat(actions)
            .element(1).`is`(org.assertj.core.api.Condition({
                it.text == key4 && it.type == ActionType.Lock
            }, ""))
        assertThat(actions)
            .element(2).`is`(org.assertj.core.api.Condition({
                it.text == key4 && it.type == ActionType.Unlock
            }, ""))

        actions.clear()
        lockWorker.withKeyExpr("x")
        val key5 =
            "AO-execution(public void io.github.caijiang.common.lock.demo.LockWorker.withKeyExpr(java.lang.String))-x this is key"
        assertThat(actions)
            .element(0).`is`(org.assertj.core.api.Condition({
                it.text == key5 && it.type == ActionType.Create
            }, ""))
        assertThat(actions)
            .element(1).`is`(org.assertj.core.api.Condition({
                it.text == key5 && it.type == ActionType.Lock
            }, ""))
        assertThat(actions)
            .element(2).`is`(org.assertj.core.api.Condition({
                it.text == key5 && it.type == ActionType.Unlock
            }, ""))


        actions.clear()
        lockWorker.withTwoLock()
        val key61 =
            "AO-lock1-null"
        val key62 =
            "AO-lock2-null"
        assertThat(actions)
            .element(0).`is`(org.assertj.core.api.Condition({
                it.text == key61 && it.type == ActionType.Create
            }, ""))
        assertThat(actions)
            .element(1).`is`(org.assertj.core.api.Condition({
                it.text == key61 && it.type == ActionType.Lock

            }, ""))

        assertThat(actions)
            .element(2).`is`(org.assertj.core.api.Condition({
                it.text == key62 && it.type == ActionType.Create
            }, ""))
        assertThat(actions)
            .element(3).`is`(org.assertj.core.api.Condition({
                it.text == key62 && it.type == ActionType.Lock
            }, ""))

        assertThat(actions)
            .element(4).`is`(org.assertj.core.api.Condition({
                it.text == key62 && it.type == ActionType.Unlock
            }, ""))
        assertThat(actions)
            .element(5).`is`(org.assertj.core.api.Condition({
                it.text == key61 && it.type == ActionType.Unlock
            }, ""))


    }

}