package io.github.caijiang.common.aliyun

import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.ICredentialProvider
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.sdk.service.alb20200616.AsyncClient
import darabonba.core.client.ClientOverrideConfiguration

/**
 * @author CJ
 */

object Helper {
    fun credentialsProvider(locator: ResourceLocator): ICredentialProvider {
        return StaticCredentialProvider.create(
            Credential.builder()
                .accessKeyId(locator.accessKeyId)
                .accessKeySecret(locator.accessKeySecret)
                //.securityToken(System.getenv("ALIBABA_CLOUD_SECURITY_TOKEN")) // use STS token
                .build()
        )
    }

    // [product_code].[region_id].aliyuncs.com
    fun createClientForProduct(productCode: String, locator: ResourceLocator): AsyncClient {
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
}