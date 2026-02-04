package io.github.caijiang.common.aliyun.oss

import com.aliyun.oss.ClientBuilderConfiguration
import com.aliyun.oss.HttpMethod
import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.common.auth.DefaultCredentialProvider
import com.aliyun.oss.common.comm.SignVersion
import com.aliyun.oss.model.*
import io.github.caijiang.common.Slf4j.Companion.log
import java.io.IOException
import java.io.InputStream
import java.time.Duration
import java.util.*

/**
 * @author CJ
 */
class OssUsageServiceImpl(private val properties: OssProperties) : OssUsageService {
    private val ossClient: OSS

    init {
        val clientBuilderConfiguration = ClientBuilderConfiguration()
        clientBuilderConfiguration.signatureVersion = SignVersion.V4
        // 进行一些检查，如果配置存在不足 则给出警告！
        ossClient = OSSClientBuilder.create().endpoint(properties.endPoint).credentialsProvider(
            DefaultCredentialProvider(
                properties.accessKey,
                properties.accessSecret
            )
        ).clientConfiguration(clientBuilderConfiguration).region(properties.region).build()

        if (properties.prvBucket.isNullOrBlank()) {
            log.warn("oss.prv-bucket 尚未配置,访问私有资源时会报告异常")
        }
        if (properties.pubBucket.isNullOrBlank()) {
            log.warn("oss.pub-bucket 尚未配置,访问公开资源时会报告异常")
        }
        if (properties.domain.isNullOrBlank()) {
            log.warn("oss.domain 尚未配置,访问公开资源时会报告异常")
        }
    }

    override fun uploadPublicResource(resourcePath: String, data: InputStream): String {
        val request = PutObjectRequest(
            properties.pubBucket ?: throw NotImplementedError("配置不支持"),
            resourcePath,
            data
        )
        val metadata = ObjectMetadata()
        metadata.setHeader("x-oss-storage-class", StorageClass.Standard.toString())
        metadata.setObjectAcl(CannedAccessControlList.PublicRead)
        request.metadata = metadata
        try {
            ossClient.putObject(request)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e)
        }
        return (properties.domain ?: throw NotImplementedError("配置不支持")) + resourcePath.removePrefix("/")
    }

    override fun publicResourceUrl(resourcePath: String): String {
        return (properties.domain ?: throw NotImplementedError("配置不支持")) + resourcePath.removePrefix("/")
    }

    override fun uploadPrivateResource(resourcePath: String, data: InputStream) {
        val request = PutObjectRequest(
            properties.prvBucket ?: throw NotImplementedError("配置不支持"),
            resourcePath,
            data
        )
        val metadata = ObjectMetadata()
        metadata.setHeader("x-oss-storage-class", StorageClass.Standard.toString())
        metadata.setObjectAcl(CannedAccessControlList.Private)
        request.metadata = metadata
        try {
            ossClient.putObject(request)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    override fun privateResourceTemporaryUrl(resourcePath: String): String {
        val request = GeneratePresignedUrlRequest(
            properties.prvBucket ?: throw NotImplementedError("配置不支持"), resourcePath, HttpMethod.GET
        )
        request.expiration = Date(
            System.currentTimeMillis() +
                    (properties.expireDuration ?: Duration.ofHours(1)).toMillis(),
        )
        try {
            val clientBuilderConfiguration = ClientBuilderConfiguration()
            clientBuilderConfiguration.signatureVersion = SignVersion.V4

            val client = OSSClientBuilder.create().endpoint(
                properties.endPoint
                    ?.replace("-internal", "")
            ).credentialsProvider(
                DefaultCredentialProvider(
                    properties.accessKey,
                    properties.accessSecret
                )
            ).clientConfiguration(clientBuilderConfiguration).region(properties.region).build()

            val signedUrl = client.generatePresignedUrl(request)
//        return StrUtil.replace(signedUrl.toString(), "+", "%2B")
            return signedUrl.toString()
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e)
        }
    }
}