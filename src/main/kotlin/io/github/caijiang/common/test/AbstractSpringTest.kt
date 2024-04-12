package io.github.caijiang.common.test

import io.github.caijiang.common.test.assertion.ResponseContentAssert
import io.github.caijiang.common.test.assertion.StdToBusinessResult
import io.github.caijiang.common.test.assertion.ToBusinessResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

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

    @Autowired
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
    protected fun assertThatResponse(data: ResponseEntity<String>): ResponseContentAssert {
        return ResponseContentAssert(data, business)
    }

    /**
     * 断言响应
     * @see updateResponseBusinessLogic
     */
    protected fun assertThatRequest(
        template: RestTemplate, uri: String, method: HttpMethod = HttpMethod.GET, entity: HttpEntity<*>? = null
    ): ResponseContentAssert {
        return assertThatResponse(template.exchange<String>(uri, method, entity))
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