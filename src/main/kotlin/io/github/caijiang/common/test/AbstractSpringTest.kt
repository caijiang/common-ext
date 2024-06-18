package io.github.caijiang.common.test

import io.github.caijiang.common.test.assertion.NormalResponseContentAssert
import io.github.caijiang.common.test.assertion.ResponseContentAssert
import io.github.caijiang.common.test.assertion.StdToBusinessResult
import io.github.caijiang.common.test.assertion.ToBusinessResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.PayloadApplicationEvent
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

/**
 * 基于 spring,junit 的测试基类
 * @since 0.0.5
 * @author CJ
 */
abstract class AbstractSpringTest {
    private val log = LoggerFactory.getLogger(AbstractSpringTest::class.java)

    companion object {
        val buildRequestFactory = try {
            RestTemplateBuilder::class.java.getMethod("buildRequestFactory")
        } catch (ex: Throwable) {
            null
        }
        var business: ToBusinessResult = StdToBusinessResult

    }

    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var environment: Environment

    /**
     * 更换处理数据的业务关系
     */
    protected fun updateResponseBusinessLogic(me: ToBusinessResult) {
        business = me
    }

    /**
     * 断言响应
     * @see updateResponseBusinessLogic
     */
    protected open fun assertThatResponse(data: ResponseEntity<String>): ResponseContentAssert<*, *> {
        return NormalResponseContentAssert(data, business)
    }

    /**
     * 断言响应
     * @see updateResponseBusinessLogic
     */
    protected open fun assertThatRequest(
        template: RestTemplate, uri: String, method: HttpMethod = HttpMethod.GET, entity: HttpEntity<*>? = null
    ): ResponseContentAssert<*, *> {
        return assertThatResponse(template.exchange<String>(uri, method, entity))
    }

    private val semaphore = Semaphore(0)
    private var releaseSemaphorePredicate: Predicate<ApplicationEvent>? = null

    @Autowired
    fun updateApplicationContext(context: ApplicationContext) {
        applicationContext = context
        (context as? ConfigurableApplicationContext)?.let { ctx ->
            ctx.addApplicationListener { event ->
                releaseSemaphorePredicate?.let {
                    if (it.test(event)) {
                        semaphore.release()
                    }
                }
            }
        }
    }

    /**
     * 让当前线程等待，直到 spring 丢出了一件你中意的事件
     * @see org.springframework.context.PayloadApplicationEvent
     * @throws InterruptedException 当前线程被噶
     * @return true 事件成功发生, false 超时了
     */
    @Throws(InterruptedException::class)
    protected fun pleaseWaitUntilEvent(
        icu: Predicate<ApplicationEvent>,
        timeout: Long = 1,
        unit: TimeUnit = TimeUnit.MINUTES
    ): Boolean {
        val current = semaphore.availablePermits()
        releaseSemaphorePredicate = icu
        return semaphore.tryAcquire(current + 1, timeout, unit)
    }

    /**
     * 跟 [pleaseWaitUntilEvent] 定义一致，区别是仅校对[PayloadApplicationEvent.payload]
     */
    @Throws(InterruptedException::class)
    protected fun pleaseWaitUntilPayloadApplicationEvent(
        icu: Predicate<Any?>,
        timeout: Long = 1,
        unit: TimeUnit = TimeUnit.MINUTES
    ): Boolean {
        return pleaseWaitUntilEvent({ event ->
            (event as? PayloadApplicationEvent<*>)?.let { icu.test(it.payload) }
                ?: false
        }, timeout, unit)
    }

    /**
     * @param requestFactoryClass 如果保持缺省的话, spring 测试运行时会根据当前的 classpath 自行选择更为合适的引擎
     */
    protected fun createTestTemplate(requestFactoryClass: Class<out ClientHttpRequestFactory>? = null): RestTemplate {
        val restTemplateBuilder = applicationContext.getBean<RestTemplateBuilder>()
        //        if (followRedirect) {
//            @Suppress("UNREACHABLE_CODE")
//                    template.requestFactory = HttpComponentsClientHttpRequestFactory()
//                    .apply {
////                httpClient = HttpClientBuilder.create()
////                    .setRedirectStrategy(LaxRedirectStrategy())
////                    .build()
//            }
//        }
        return restTemplateBuilder
            .errorHandler(object : ResponseErrorHandler {
                override fun hasError(response: ClientHttpResponse): Boolean {
                    return false
                }

                override fun handleError(response: ClientHttpResponse) {
                }
            })
            .uriTemplateHandler(LocalHostUriTemplateHandler(environment))
            .run {
                if (requestFactoryClass == null) {
                    val current = buildRequestFactory?.invoke(this)
                    if (current != null && current.javaClass.simpleName.startsWith("OkHttp", true)) {
                        log.warn("OkHttp will lost cookie.")
                    }
                    this
                } else {
                    requestFactory(requestFactoryClass)
                }
            }
            .build()
    }


}