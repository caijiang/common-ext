package io.github.caijiang.common

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

/**
 * 协程技术性测试
 * @author CJ
 */
@Suppress("TestFunctionName", "NonAsciiCharacters", "UNREACHABLE_CODE")
class CoroutineTechTest {

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun 异常在协程中的行为() {
        runBlocking {
            val job = GlobalScope.launch { // root coroutine with launch
                println("即将抛出异常")
                throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
            }
            job.join()
            println("join() 会正常执行，因为 launch 是不追求结果的 ")
            val deferred = GlobalScope.async { // root coroutine with async
                println("即将抛出异常")
                throw ArithmeticException() // Nothing is printed, relying on user to call await
            }
            try {
                deferred.await()
                println("await() 就会抛出原异常,因为 async 是追求结果的")
            } catch (e: ArithmeticException) {
                println("Caught ArithmeticException")
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun 配合CoroutineExceptionHandler() {
        runBlocking {
            val job = GlobalScope.launch(CoroutineExceptionHandler { _, b ->
                println("CoroutineExceptionHandler 会抓住 launch 中的未捕捉异常 $b")
            }) { // root coroutine with launch
                println("即将抛出异常")
                throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
            }
            job.join()
            println("join() 会正常执行，因为 launch 是不追求结果的 ")
            val deferred = GlobalScope.async(
                CoroutineExceptionHandler { _, throwable ->
                    println("CoroutineExceptionHandler 抓不住 async 中的异常 $throwable")
                }
            ) { // root coroutine with async
                println("即将抛出异常")
                throw ArithmeticException() // Nothing is printed, relying on user to call await
            }
            try {
                deferred.await()
                println("await() 就会抛出原异常,因为 async 是追求结果的")
            } catch (e: ArithmeticException) {
                println("Caught ArithmeticException")
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun 继续测试异常的传染性() {
        runBlocking {
            val job = GlobalScope.launch(CoroutineExceptionHandler { _, b ->
                println("CoroutineExceptionHandler 会抓住 launch 中的未捕捉异常 $b")
            }) { // root coroutine with launch
                withContext(Dispatchers.IO) {
                    println("即将抛出异常")
                    throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
                }
            }
            job.join()
            println("join() 会正常执行，因为 launch 是不追求结果的 ")
            val deferred = GlobalScope.async(
                CoroutineExceptionHandler { _, throwable ->
                    println("CoroutineExceptionHandler 抓不住 async 中的异常 $throwable")
                }
            ) { // root coroutine with async
                println("即将抛出异常")
                throw ArithmeticException() // Nothing is printed, relying on user to call await
            }
            try {
                deferred.await()
                println("await() 就会抛出原异常,因为 async 是追求结果的")
            } catch (e: ArithmeticException) {
                println("Caught ArithmeticException")
            }
        }
    }

}