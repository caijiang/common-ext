package io.github.caijiang.common.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.web.client.RestTemplate

/**
 * 基于 spring,junit 的测试基类
 * @since 0.0.5
 * @author CJ
 */
abstract class AbstractSpringTest {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var environment: Environment

    protected fun createTestTemplate(): RestTemplate {
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
            .build()
    }


}