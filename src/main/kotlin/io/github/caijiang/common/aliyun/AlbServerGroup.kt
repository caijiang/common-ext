package io.github.caijiang.common.aliyun

import com.aliyun.sdk.service.alb20200616.AsyncClient
import com.aliyun.sdk.service.alb20200616.models.*
import io.github.caijiang.common.Slf4j
import io.github.caijiang.common.logging.LoggingApi
import io.github.caijiang.common.orchestration.IngressEntrance
import io.github.caijiang.common.orchestration.Service
import io.github.caijiang.common.orchestration.ServiceNode
import org.springframework.boot.logging.LogLevel

private val albProduct = "alb" to {
    AsyncClient.builder()
}

/**
 * 阿里云的 alb 服务器组
 * @author CJ
 */
@Slf4j
class AlbServerGroup(
    /**
     * 服务器组 ID。
     */
    private val groupId: String,
    private val locator: ResourceLocator,
    /**
     * 可以获知该服务器组是否正常工作的监听 id
     */
    private val listenerId: String? = null,
) : IngressEntrance {
    override val ingressName: String
        get() = "阿里云ALB服务器组($groupId)"

    override fun suspendNode(serviceNode: ServiceNode, loggingApi: LoggingApi) {
        val ecs = serviceNode as? EcsNodeInAlbGroup ?: return
        if (ecs.weight <= 0) {
            loggingApi.logMessage(LogLevel.INFO, "${ecs.serverIp} 本身就没有流量，无需暂停")
            return
        }
        changeWeight(loggingApi) {
            if (it.serverId == ecs.serverId) 0 else null
        }
    }

    private var justResumed = false

    override fun resumedNode(serviceNode: ServiceNode, loggingApi: LoggingApi) {
        val ecs = serviceNode as? EcsNodeInAlbGroup ?: return
        val weight = lastList.filter { it.serverId == ecs.serverId }.map { it.weight }.firstOrNull() ?: return
        changeWeight(loggingApi) {
            if (it.serverId == ecs.serverId) weight else null
        }
        justResumed = true
    }

    override fun checkWorkStatus(node: ServiceNode, loggingApi: LoggingApi): Boolean {
        if (listenerId == null) {
            loggingApi.logMessage(LogLevel.WARN, "服务器组:${groupId} 没有关联监听，无法获知是否正常工作")
            return true
        }
        if (justResumed) {
            justResumed = false
            loggingApi.logMessage(LogLevel.DEBUG, "刚刚尝试了恢复流量，阿里云反应迟钝，这里暂停 一分钟再检查流量状态")
            Thread.sleep(60000)
        }
        Helper.createClientForProduct(albProduct, locator)
            .use { client ->
                var nextToken: String? = null
                val list = mutableListOf<GetListenerHealthStatusResponseBody.ServerGroupInfos>()
                while (true) {
                    val request = GetListenerHealthStatusRequest.builder()
                        .listenerId(listenerId)
                        .nextToken(nextToken)
                        .build()

                    val response = client.getListenerHealthStatus(request).get()

                    response.body?.listenerHealthStatus?.forEach { healthStatus ->
                        healthStatus.serverGroupInfos?.let {
                            list.addAll(it)
                        }
                    }

                    if (response.body.nextToken?.isNotBlank() == true) {
                        nextToken = response.body.nextToken
                    } else
                        break
                }

                return list.asSequence().filter { it.serverGroupId == groupId }
                    .filter { it.nonNormalServers != null }
                    .flatMap { it.nonNormalServers }
                    .filter { it.serverIp == node.ip }
                    .onEach {
                        loggingApi.logMessage(LogLevel.DEBUG, "ip:${node.ip}, status:${it.status}, reason:${it.reason}")
                    }
                    .count() == 0
                //
            }

    }


    private val lastList: MutableList<EcsNodeInAlbGroup> = mutableListOf()

    override fun discoverNodes(service: Service): List<ServiceNode> {
        return Helper.createClientForProduct(albProduct, locator)
            .use { client ->
                val servers = readServers(client)
                val list = servers
                    .filter { it.serverType == "Ecs" }
                    .filter { it.status == "Available" }
                    .map {
                        EcsNodeInAlbGroup(
                            serverId = it.serverId,
                            serverGroupId = it.serverGroupId,
                            serverIp = it.serverIp,
                            port = it.port,
                            weight = it.weight,
                            description = it.description,
                        )
                    }

                lastList.clear()
                lastList.addAll(list)

                list
            }
    }

    private fun readServers(client: AsyncClient): List<ListServerGroupServersResponseBody.Servers> {
        val list = mutableListOf<ListServerGroupServersResponseBody.Servers>()
        var nextToken: String? = null
        while (true) {
            // Parameter settings for API request
            val listServerGroupServersRequest = ListServerGroupServersRequest.builder()
                .serverGroupId(groupId) // Request-level configuration rewrite, can set Http request parameters, etc.
                // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                .nextToken(nextToken)
                .build()

            // Asynchronously get the return value of the API request
            val response = client.listServerGroupServers(listServerGroupServersRequest)

            // Synchronously get the return value of the API request
            val resp = response.get()

            resp.body.servers?.let { list.addAll(it) }

            if (resp.body.nextToken?.isNotBlank() == true) {
                nextToken = resp.body.nextToken
            } else
                break
        }

        return list
    }

    private fun changeWeight(
        loggingApi: LoggingApi,
        weightFunction: (ListServerGroupServersResponseBody.Servers) -> Int?
    ) {
        Helper.createClientForProduct(albProduct, locator)
            .use { client ->
                val servers = readServers(client)

                if (servers.all { weightFunction(it) == null }) {
                    loggingApi.logMessage(LogLevel.DEBUG, "没有任何节点符合当前服务器组")
                    return
                }

                // Parameter settings for API request
                val updateServerGroupServersAttributeRequest =
                    UpdateServerGroupServersAttributeRequest.builder() // Request-level configuration rewrite, can set Http request parameters, etc.
                        // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                        .serverGroupId(groupId)
                        .servers(
                            servers
                                .map {
                                    val iw = weightFunction(it)
                                    UpdateServerGroupServersAttributeRequest.Servers
                                        .builder()
                                        .description(it.description)
                                        .port(it.port)
                                        .serverId(it.serverId)
                                        .serverIp(it.serverIp)
                                        .serverType(it.serverType)
                                        .weight(iw ?: it.weight)
                                        .build()
                                }
                        )
                        .build()


                // Asynchronously get the return value of the API request
                val response = client.updateServerGroupServersAttribute(updateServerGroupServersAttributeRequest)

                // Synchronously get the return value of the API request
                response.get()
            }
    }

}