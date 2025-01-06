package io.github.caijiang.common.nacos

/**
 * @author CJ
 */
data class ResourceLocator(
    /**
     * 类似 localhost:8848
     */
    val serverAddr: String,
    /**
     * 执行 `/nacos/v1/auth/login` 的数据；可缺省
     */
    val auth: AuthData? = null,
    /**
     * 自行设定 token
     */
    val accessToken: String? = null,
    /**
     * 命名空间Id，默认为`public`
     */
    val namespaceId: String? = null,
    /**
     * 分组名，默认为`DEFAULT_GROUP`
     */
    val groupName: String? = null,
    /**
     * 集群名称，默认为`DEFAULT`
     */
    val clusterName: String? = null,
    val api: NacosApiVersion = NacosApiVersion.V23,
)

data class AuthData(
    val username: String,
    val password: String
)
