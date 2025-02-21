package io.github.caijiang.common.nacos

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.orchestration.HealthCheck
import io.github.caijiang.common.orchestration.Service
import io.github.caijiang.common.orchestration.ServiceNode
import org.junit.jupiter.api.Disabled
import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml
import kotlin.test.Test

/**
 * @author CJ
 */
@Disabled
class NacosServiceTest {

    /**
     * 本地开发环境的测试
     */
    @Test
    fun forLocal() {
        val targetNode = object : ServiceNode {
            override val ip: String
                get() = "10.0.3.6"
            override val port: Int
                get() = 8080
            override val ingressLess: Boolean
                get() = false
        }


        val nacosService = NacosService(
            "ph-spring-boot-demo-base-8",
            ResourceLocator(
                "192.168.16.208:8848",
                fetchResourceLocator("local-nacos-data-dev.yaml"),
                api = NacosApiVersion.V1x,
                namespaceId = "dev"
            ),
        )

        caseFor(nacosService, targetNode)
    }

    /**
     * 在线的一个 demo
     */
    @Test
    fun forOnlineDemo() {
        val targetNode = object : ServiceNode {
            override val ip: String
                get() = "172.17.0.8"
            override val port: Int
                get() = 8080
            override val ingressLess: Boolean
                get() = false
        }

        val nacosService = NacosService(
            "ph-spring-boot-demo-base-8",
            ResourceLocator(
                "mse-c8251100-nacos-ans.mse.aliyuncs.com:8848",
                api = NacosApiVersion.V23,
                namespaceId = "dev",
                accessKey = fetchResourceLocator("local-nacos-data-online.yaml").username,
                secretKey = fetchResourceLocator("local-nacos-data-online.yaml").password
            ),
        )

        caseFor(nacosService, targetNode)
    }

    private fun fetchResourceLocator(path: String = "local-nacos-data.yaml"): AuthData {
        val data = Yaml().load<Map<String, String>>(ClassPathResource(path).inputStream)
        return AuthData(
            username = data["username"].toString(),
            password = data["password"].toString(),
        )
    }

    /**
     * 经典生产环境
     */
    @Test
    fun forPhOnline() {
        val targetNode = object : ServiceNode {
            override val ip: String
                get() = "172.16.208.93"
            override val port: Int
                get() = 8666
            override val ingressLess: Boolean
                get() = false
        }

        val nacosService = NacosService(
            "ph-spring-boot-demo-base-8",
            ResourceLocator(
                "127.0.0.1:8848",
                fetchResourceLocator(),
                api = NacosApiVersion.V1x,
                namespaceId = "bb7b30b2-202e-41e8-90fc-8a1f69ad9332"
            ),
        )

        caseFor(nacosService, targetNode)
    }

    private fun caseFor(
        nacosService: NacosService,
        targetNode: ServiceNode
    ) {
        val list = nacosService.discoverNodes(object : Service {
            override val id: String
                get() = "ph-spring-boot-demo-base-8"
            override val type: String
                get() = TODO("Not yet implemented")
            override val deployCommand: String
                get() = TODO("Not yet implemented")
            override val healthCheck: HealthCheck
                get() = TODO("Not yet implemented")
            override val environment: Map<String, String>?
                get() = null
        })

        println(list.map { "${it.ip}:${it.port}  but:${it.ingressLess}" })

        while (true) {
            try {
                nacosService.suspendNode(targetNode)
                println("已暂停流量，请观察")
                Thread.sleep(3000)
                break
            } catch (e: Exception) {
                Thread.sleep(3000)
                log.info("ex", e)
            }
        }

        while (true) {
            try {
                nacosService.resumedNode(targetNode)
                println("已恢复流量，请观察")
                Thread.sleep(3000)
                break
            } catch (e: Exception) {
                Thread.sleep(3000)
                log.info("ex", e)
            }
        }

        while (true) {
            if (nacosService.checkWorkStatus(targetNode))
                break
            Thread.sleep(3000)
        }
    }
}