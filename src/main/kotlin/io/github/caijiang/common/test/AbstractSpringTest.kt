package io.github.caijiang.common.test

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

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

    }

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var environment: Environment

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