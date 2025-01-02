package io.github.caijiang.common.nacos

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.orchestration.HealthCheck
import io.github.caijiang.common.orchestration.Service
import io.github.caijiang.common.orchestration.ServiceNode
import kotlin.test.Test

/**
 * @author CJ
 */
class NacosServiceTest {
    @Test
    fun test() {
        val nacosService = NacosService(
            "ph-spring-boot-demo-base-8",
            ResourceLocator("localhost:8848")
        )

        val list = nacosService.discoverNodes(object : Service {
            override val id: String
                get() = "ph-spring-boot-demo-base-8"
            override val type: String
                get() = TODO("Not yet implemented")
            override val deployCommand: String
                get() = TODO("Not yet implemented")
            override val healthCheck: HealthCheck
                get() = TODO("Not yet implemented")
        })

        println(list.map { "${it.ip}:${it.port}  but:${it.ingressLess}" })

        val targetNode = object : ServiceNode {
            override val ip: String
                get() = "192.168.2.37"
            override val port: Int
                get() = 8080
            override val ingressLess: Boolean
                get() = false
        }

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