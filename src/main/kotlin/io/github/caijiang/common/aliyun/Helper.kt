package io.github.caijiang.common.aliyun

import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.ICredentialProvider
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.sdk.service.alb20200616.AsyncClient
import com.aliyuncs.CommonRequest
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.profile.IClientProfile
import darabonba.core.client.ClientOverrideConfiguration
import io.github.caijiang.common.Slf4j.Companion.log


/**
 * @author CJ
 */

object Helper {

    private fun commonProfileFrom(locator: ResourceLocator): IClientProfile {
        return DefaultProfile.getProfile(
            locator.region,
            locator.accessKeyId, locator.accessKeySecret,
        )
    }

    private fun commonDefaultAcsClientFrom(locator: ResourceLocator) = DefaultAcsClient(commonProfileFrom(locator))

    private fun credentialsProvider(locator: ResourceLocator): ICredentialProvider {
        return StaticCredentialProvider.create(
            Credential.builder()
                .accessKeyId(locator.accessKeyId)
                .accessKeySecret(locator.accessKeySecret)
                //.securityToken(System.getenv("ALIBABA_CLOUD_SECURITY_TOKEN")) // use STS token
                .build()
        )
    }

    fun commonEssRequest(): CommonRequest {
        val request = CommonRequest()
        request.sysMethod = MethodType.POST
        request.sysDomain = "ess.aliyuncs.com"
        request.sysVersion = "2014-08-28"
        return request
    }

    // [product_code].[region_id].aliyuncs.com
    fun createClientForProduct(productCode: String, locator: ResourceLocator): AsyncClient {
        // BaseClientBuilder, IClientBuilder, DefaultClientBuilder
        val vpc = "1" == System.getenv("VPC")
        val ep = "$productCode${if (vpc) "-vpc" else ""}.${locator.region}.aliyuncs.com"
        return AsyncClient.builder()
            .region(locator.region) // Region ID
            //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
            .credentialsProvider(credentialsProvider(locator)) //.serviceConfiguration(Configuration.create()) // Service-level configuration
            // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
            .overrideConfiguration(
                ClientOverrideConfiguration.create() // Endpoint 请参考 https://api.aliyun.com/product/Alb
                    .setEndpointOverride(ep)
                //.setConnectTimeout(Duration.ofSeconds(30))
            )
            .build()
    }

    fun executeCommonRequest(locator: ResourceLocator, request: CommonRequest): String? {
        val client = commonDefaultAcsClientFrom(locator)
        val response = client.getCommonResponse(request)

        if (!response.httpResponse.isSuccess) {
            log.debug("response.data:{}", response.data)
            throw IllegalStateException("Error response code ${response.httpStatus}:${response.httpResponse.reasonPhrase}")
        }
        return response.data
    }
}