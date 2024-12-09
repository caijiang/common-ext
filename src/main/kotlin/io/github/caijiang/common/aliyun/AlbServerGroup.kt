package io.github.caijiang.common.aliyun

import com.aliyun.sdk.service.alb20200616.AsyncClient
import com.aliyun.sdk.service.alb20200616.models.ListServerGroupServersRequest
import com.aliyun.sdk.service.alb20200616.models.ListServerGroupServersResponseBody
import com.aliyun.sdk.service.alb20200616.models.UpdateServerGroupServersAttributeRequest
import io.github.caijiang.common.Slf4j
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.orchestration.IngressEntrance
import io.github.caijiang.common.orchestration.Service
import io.github.caijiang.common.orchestration.ServiceNode


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
) : IngressEntrance {
    override fun suspendNode(serviceNode: ServiceNode) {
        val ecs = serviceNode as? EcsNodeInAlbGroup ?: return
        if (ecs.weight <= 0) {
            log.info("{} 本身就没有流量，无需暂停", ecs.serverIp)
            return
        }
        changeWeight {
            if (it.serverId == ecs.serverId) 0 else null
        }
    }

    override fun resumedNode(serviceNode: ServiceNode) {
        val ecs = serviceNode as? EcsNodeInAlbGroup ?: return
        val weight = lastList.filter { it.serverId == ecs.serverId }.map { it.weight }.firstOrNull() ?: return
        changeWeight {
            if (it.serverId == ecs.serverId) weight else null
        }
    }


    private val lastList: MutableList<EcsNodeInAlbGroup> = mutableListOf()

    override fun discoverNodes(service: Service): List<ServiceNode> {
        return Helper.createClientForProduct("alb", locator)
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

    private fun readServers(client: AsyncClient): MutableList<ListServerGroupServersResponseBody.Servers> {
        // Parameter settings for API request
        val listServerGroupServersRequest = ListServerGroupServersRequest.builder()
            .serverGroupId(groupId) // Request-level configuration rewrite, can set Http request parameters, etc.
            // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
            .build()

        // Asynchronously get the return value of the API request
        val response = client.listServerGroupServers(listServerGroupServersRequest)

        // Synchronously get the return value of the API request
        val resp = response.get()

        val servers = resp.body.servers
        return servers ?: mutableListOf()
    }

    private fun changeWeight(weightFunction: (ListServerGroupServersResponseBody.Servers) -> Int?) {
        Helper.createClientForProduct("alb", locator)
            .use { client ->
                val servers = readServers(client)

                if (servers.all { weightFunction(it) == null }) {
                    log.debug("没有任何节点符合当前服务器组")
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