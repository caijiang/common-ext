package io.github.caijiang.common.aliyun

import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.ICredentialProvider
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.core.utils.SdkAutoCloseable
import com.aliyuncs.CommonRequest
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import com.aliyuncs.profile.IClientProfile
import darabonba.core.client.ClientOverrideConfiguration
import darabonba.core.client.IClientBuilder
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

    fun commonDefaultAcsClientFrom(locator: ResourceLocator) = DefaultAcsClient(commonProfileFrom(locator))

    private fun credentialsProvider(locator: ResourceLocator): ICredentialProvider {
        val builder = Credential.builder()
        val credential = (locator.securityToken?.let { builder.securityToken(it) } ?: builder
            .accessKeyId(locator.accessKeyId)
            .accessKeySecret(locator.accessKeySecret))
            .build()

        return StaticCredentialProvider.create(
            credential
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
    fun <T : SdkAutoCloseable, BUILDER : IClientBuilder<BUILDER, T>> createClientForProduct(
        product: Pair<String, () -> BUILDER>,
        locator: ResourceLocator,
        toEndpoint: (productName: String) -> String = {
            val productCode = product.first
            val vpc = "1" == System.getenv("VPC")
            "$productCode${if (vpc) "-vpc" else ""}.${locator.region}.aliyuncs.com"
        },
    ): T {
        return product.second.invoke()
            .region(locator.region) // Region ID
            //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
            .credentialsProvider(credentialsProvider(locator)) //.serviceConfiguration(Configuration.create()) // Service-level configuration
            // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
            .overrideConfiguration(
                ClientOverrideConfiguration.create() // Endpoint 请参考 https://api.aliyun.com/product/Alb
                    .setEndpointOverride(toEndpoint(product.first))
                //.setConnectTimeout(Duration.ofSeconds(30))
            )
            .build()
    }

    /**
     * 阿里云 nextToken 风格的分页查询
     * @param responseForNextToken 执行请求，根据入参的 token 返回本次响应
     * @param responseToNextToken 基于响应获得下一次的 token
     * @param responseToElements 基于响应获得本次的项目列表
     * @param stopCondition 可选的参数，提前结束的条件
     * @return 合并的结果
     */
    fun <ELEMENT, RESPONSE> tokenPageRequestForAsyncClient(
        responseForNextToken: (nextToken: String?) -> RESPONSE,
        responseToElements: (response: RESPONSE) -> List<ELEMENT>,
        responseToNextToken: (response: RESPONSE) -> String?,
        stopCondition: ((current: List<ELEMENT>) -> Boolean)? = null,
    ): List<ELEMENT> {
        var nextToken: String? = null
        val list = mutableListOf<ELEMENT>()
        while (true) {
            if (stopCondition?.invoke(list) == true) {
                return list
            }
            val response = responseForNextToken.invoke(nextToken)

            list.addAll(responseToElements(response))

            val nx = responseToNextToken(response)
            if (nx?.isNotBlank() == true) {
                nextToken = nx
            } else
                break
        }
        return list
    }

    fun executeCommonRequest(locator: ResourceLocator, request: CommonRequest): String? {
        val client = commonDefaultAcsClientFrom(locator)
        val response = client.getCommonResponse(request)

        log.debug("Executed common request: {}:{}:{} ", request.sysAction, request.sysQueryParameters, response.data)

        if (!response.httpResponse.isSuccess) {
            log.debug("response.data:{}", response.data)
            throw IllegalStateException("Error response code ${response.httpStatus}:${response.httpResponse.reasonPhrase}")
        }
        return response.data
    }
}