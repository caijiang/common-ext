package io.github.caijiang.common.security

import java.security.*
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*


/**
 * ## 生成稳定私钥
 * ```shell
 * openssl genpkey -out rsa_key.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048
 * ```
 * @author CJ
 */
object RSAUtils {

    /**
     * @return 随机私钥对
     */
    @JvmStatic
    @Throws(InvalidParameterException::class)
    fun generatePrivateKey(keySize: Int = 2048): KeyPair {
        return KeyPairGenerator.getInstance("RSA").apply {
            initialize(keySize)
        }.generateKeyPair()
    }

    /**
     * @param key pem(pkcs8) 格式的rsa 密钥
     */
    @Throws(InvalidKeySpecException::class)
    @JvmStatic
    fun readPrivateKeyFromPKCS8(key: String): RSAPrivateKey {
        val pem = key
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PRIVATE KEY-----", "")

        val encoded: ByteArray = Base64.getDecoder().decode(pem)

        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    /**
     * @since 1.3.0
     * @param key pem(pkcs8) 格式的rsa 密钥
     * @see readPrivateKeyFromPKCS8
     * @return KeyPair
     */
    @Throws(InvalidKeySpecException::class)
    @JvmStatic
    fun readKeyPairFromPKCS8(key: String): KeyPair {
        val privateKey = readPrivateKeyFromPKCS8(key)
        return KeyPair(
            extraPublicKey(privateKey), privateKey
        )
    }

    /**
     * 用 SHA256withRSA 签名
     * @return 签名并且 base64 后的字符
     */
    @Throws(InvalidKeySpecException::class)
    @JvmStatic
    fun signatureAsBase64(key: PrivateKey, payload: ByteArray): String {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(key)
        signature.update(payload)
        val result = signature.sign()
        return Base64.getEncoder().encodeToString(result)
    }

    /**
     * 使用 SHA256withRSA 验签
     * @param publicKey 公钥
     * @param payload 数据
     * @param signatureBase64 base64后的签名
     */
    @Throws(InvalidKeyException::class, SignatureException::class)
    @JvmStatic
    fun verify(publicKey: PublicKey, payload: ByteArray, signatureBase64: String): Boolean {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(payload)
        return signature.verify(Base64.getDecoder().decode(signatureBase64))
    }

    private fun extraPublicKey(privateKey: RSAPrivateKey): PublicKey {
        return KeyFactory.getInstance("RSA").generatePublic(
            RSAPublicKeySpec(
                privateKey.modulus, (privateKey as RSAPrivateCrtKey).publicExponent
            )
        )
    }

    /**
     * 从私钥中读取公钥并且写入到 pem(x509)格式
     */
    @Throws(InvalidKeySpecException::class)
    @JvmStatic
    fun readPublicKeyPem(privateKey: RSAPrivateKey): String {
        val pk = extraPublicKey(privateKey)
        return Base64.getEncoder().encodeToString(pk.encoded)
    }

    /**
     * @param key pem(x509)格式的公钥
     */
    @Throws(InvalidKeySpecException::class)
    @JvmStatic
    fun readPublicKey(key: String): RSAPublicKey {
        val pem = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PUBLIC KEY-----", "")

        val encoded = Base64.getDecoder().decode(pem)
        val publicKeySpec = X509EncodedKeySpec(encoded)
        val factory = KeyFactory.getInstance("RSA")
        return factory.generatePublic(publicKeySpec) as RSAPublicKey
    }

}