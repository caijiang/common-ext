package io.github.caijiang.common.security

import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import java.io.File
import kotlin.test.Test


/**
 * ## 生成稳定私钥
 * ```shell
 * openssl genpkey -out rsa_key.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048
 * ```
 * @author CJ
 */
@EnabledOnOs(OS.MAC)
class RSAUtilsTest {
    @Test
    fun go() {
        val gpk = RSAUtils.generatePrivateKey().private
        println(gpk)

        val keyFile = File.createTempFile("java", ".pem")
        keyFile.deleteOnExit()
        val process =
            Runtime.getRuntime().exec("openssl genpkey -out $keyFile -algorithm RSA -pkeyopt rsa_keygen_bits:2048")
        process.waitFor()

        val privateKey = RSAUtils.readPrivateKeyFromPKCS8(keyFile.readText(Charsets.UTF_8))
        println(privateKey)

//        随机数据
        val payload = RandomStringUtils.randomAlphabetic(32).toByteArray()

        val signature = RSAUtils.signatureAsBase64(privateKey, payload)
        println(signature)

        val publicKeyPem = RSAUtils.readPublicKeyPem(privateKey)
        println(publicKeyPem)

        val publicKey = RSAUtils.readPublicKey(publicKeyPem)
        println(publicKey)

        assertThat(
            RSAUtils.verify(publicKey, payload, signature)
        )
            .isTrue()

    }
}