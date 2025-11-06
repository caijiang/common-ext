@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package io.github.caijiang.common.aliyun.oss

import io.github.caijiang.common.aliyun.oss.test.OssApp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.io.ByteArrayInputStream
import java.net.URL


/**
 * @author CJ
 */
@SpringBootTest(classes = [OssApp::class])
class OssUsageAutoConfigurationDefaultTest {
    @Test
    internal fun 逃过自动载入(@Autowired(required = false) service: OssUsageAutoConfiguration?) {
        assertThat(service)
            .`as`("没有特别声明，我们不会载入")
            .isNull()
    }
}

@SpringBootTest(classes = [OssApp::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = [OptionalYamlContextInitializer::class])
class OssUsageAutoConfigurationTest {

    @BeforeAll
    fun checkFileExists() {
        // 🟡 如果文件不存在 → 跳过整个测试类
        assumeTrue(OptionalYamlContextInitializer.resource.exists()) {
            "Skipping test: ${OptionalYamlContextInitializer.resource} not found"
        }
    }

    @Test
    internal fun 肯定自动载入(@Autowired(required = false) service: OssUsageAutoConfiguration?) {
        assertThat(service).isNotNull
    }

    @Test
    internal fun 各种业务走一下(@Autowired(required = false) service: OssUsageService?) {
        assertThat(service).isNotNull

        val data = byteArrayOf(1)
        val privateResourcePath = "1.img"
        service!!.uploadPrivateResource(privateResourcePath, ByteArrayInputStream(data))
        println(service.uploadPrivateResourceForUrl(privateResourcePath, ByteArrayInputStream(data)))
        val temporaryUrl = service.privateResourceTemporaryUrl(privateResourcePath)
        assertThat(temporaryUrl)
            .isNotNull()
            .isNotEmpty()
        assertThat(URL(temporaryUrl).openConnection().getInputStream().readBytes())
            .containsOnly(data.toTypedArray())


        val publicResourcePath = "2.img"
        val publicUrl = service.uploadPublicResource(publicResourcePath, ByteArrayInputStream(data))
        println(publicUrl)
        assertThat(publicUrl)
            .isNotNull()
            .isNotEmpty()

        assertThat(URL(publicUrl).openConnection().getInputStream().readBytes())
            .containsOnly(data.toTypedArray())
        assertThat(service.publicResourceUrl(publicResourcePath))
            .isEqualTo(publicUrl)

    }
}