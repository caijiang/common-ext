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

    @Test
    fun forLocal() {
        val targetNode = object : ServiceNode {
            override val ip: String
                get() = "192.168.2.37"
            override val port: Int
                get() = 8080
            override val ingressLess: Boolean
                get() = false
        }


        val nacosService = NacosService(
            "ph-spring-boot-demo-base-8",
            ResourceLocator("localhost:8848")
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
                get() = "172.16.208.89"
            override val port: Int
                get() = 8080
            override val ingressLess: Boolean
                get() = false
        }

        val nacosService = NacosService(
            "ph-spring-boot-demo-base-8",
            ResourceLocator(
                "mse-c8251100-nacos-ans.mse.aliyuncs.com:8848",
                null,
                api = NacosApiVersion.V23,
                namespaceId = "bb7b30b2-202e-41e8-90fc-8a1f69ad9332"
            ),
        )

        caseFor(nacosService, targetNode)
    }

    private fun fetchResourceLocator(): AuthData {
        val data = Yaml().load<Map<String, String>>(ClassPathResource("local-nacos-data.yaml").inputStream)
        return AuthData(
            username = data["username"].toString(),
            password = data["password"].toString(),
        )
    }

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
                break
            } catch (e: Exception) {
                Thread.sleep(3000)
                log.info("ex", e)
            }
        }

        while (true) {
            try {
                nacosService.resumedNode(targetNode)
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