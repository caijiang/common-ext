@file:OptIn(ExperimentalSerializationApi::class)

package io.github.caijiang.common.job.scheduler

import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.github.caijiang.common.job.worker.PersistentJob
import io.github.caijiang.common.job.worker.TemporaryJob
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.jupiter.api.Disabled
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.*
import kotlin.test.Test


private val json = Json {
    ignoreUnknownKeys = true
}

/**
 * 默认 /var/run/secrets/kubernetes.io/serviceaccount/token
 */
fun workWithLocalKubernetesCluster(javaClass: Class<Any>, block: KubernetesClient.() -> Unit) {
    javaClass.getResourceAsStream("/local-build-k8s-cluster-config.json")?.let { inputStream ->
        val data = inputStream.use {
            json.decodeFromStream<K8sClusterConfig>(it)
        }
        val portWork = try {
            ServerSocket(data.port).use {
                it.reuseAddress = true
                it.bind(InetSocketAddress(data.port))
            }
            false
        } catch (e: IOException) {
            true
        }

        if (!portWork) {
            println("本地集群依赖端口:${data.port}但是没有发现，跳过执行")
            return
        }

        KubernetesClientBuilder().withConfig(
            ConfigBuilder()
                .withMasterUrl("https://localhost:${data.port}/")
                .withTrustCerts(true)
                .withAutoOAuthToken(data.autoOAuthToken)
                .build()
        ).build().apply(block)
    }
}

/**
 * @author CJ
 */
@Disabled
class KubernetesJobSchedulerTest {
    @Test
    fun test() {
        workWithLocalKubernetesCluster(javaClass) {
            val scheduler = KubernetesJobScheduler(this)

            scheduler.submitTemporaryJob(
                "test-ns", "echo-server-7445cd987f-s9r6b", object : TemporaryJob {
                    override val type: String
                        get() = "test-job"
                    override val parameters: Map<String, String>
                        get() = mapOf(
                            "k1" to "value1",
                            "k2" to "value2"
                        )
                }
            )

            scheduler.submitPersistentJob(
                "test-ns", "echo-server-7445cd987f-s9r6b", "* * * * *", object : PersistentJob {
                    override val name: String
                        get() = "test-cron-job"
                    override val type: String
                        get() = "test-job-2"
                    override val parameters: Map<String, String>
                        get() = mapOf(
                            "k1" to "value1",
                            "k2" to "value-not-2-2"
                        )
                }, TimeZone.getDefault()
            )
        }
    }
}