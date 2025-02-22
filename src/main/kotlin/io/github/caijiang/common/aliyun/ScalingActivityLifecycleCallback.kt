package io.github.caijiang.common.aliyun

import com.aliyuncs.CommonRequest
import com.aliyuncs.ecs.model.v20140526.DescribeInstancesResponse
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.caijiang.common.Slf4j
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.aliyun.model.LifecycleAction
import io.github.caijiang.common.aliyun.model.LifecycleActionResult
import io.github.caijiang.common.aliyun.model.ScalingActivity
import io.github.caijiang.common.aliyun.tool.EciTool
import io.github.caijiang.common.aliyun.tool.EcsTool

private val DescribeInstancesResponse.Instance.intranetIp: String?
    get() {
        if (!this.innerIpAddress.isNullOrEmpty())
            return innerIpAddress.first()
        if (!vpcAttributes.privateIpAddress.isNullOrEmpty())
            return vpcAttributes.privateIpAddress.first()
        return null
    }

/**
 * 阿里云，伸缩活动，生命周期回调
 * https://help.aliyun.com/zh/auto-scaling/developer-reference/api-describelifecycleactions?spm=a2c4g.11186623.help-menu-25855.d_5_0_0_14_4.6f1e2afeLEtGsD
 * @author CJ
 */
@Slf4j
object ScalingActivityLifecycleCallback {
    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule())

    // Iterable
    private fun JsonNode.nodesIn(vararg paths: String): JsonNode {
        var current = this
        for (path in paths) {
            val next = current[path]
            if (next == null || next.isNull) return objectMapper.createArrayNode()
            current = next
        }
        return current
    }

    private inline fun <reified T> readListFromRequest(
        locator: ResourceLocator,
        request: CommonRequest,
        vararg paths: String
    ): List<T> {
        val rs = Helper.executeCommonRequest(locator, request)
            ?: throw IllegalStateException("no data from ${request.sysAction}:${request.sysQueryParameters}")

        val valueReader = objectMapper.readerFor(T::class.java)
        val list = objectMapper.readTree(rs).nodesIn(*paths)
            .map {
                valueReader.readValue(it, T::class.java)
            }
        return list
    }

    /**
     * @param locator 阿里云
     * @param scalingGroupId 伸缩组 id
     * @return 获取所有生命周期挂钩
     */
    fun queryLifecycleActions(
        locator: ResourceLocator,
        scalingGroupId: String
    ): Map<ScalingActivity, List<LifecycleAction>> {
        val request = Helper.commonEssRequest()

        // 获取 伸缩活动
        //  https://help.aliyun.com/zh/auto-scaling/developer-reference/api-describescalingactivities?spm=a2c4g.11186623.help-menu-25855.d_5_0_0_7_8.4471637bUNvxq4
        request.sysAction = "DescribeScalingActivities"
        request.putQueryParameter("RegionId", locator.region)
        request.putQueryParameter("ScalingGroupId", scalingGroupId)
        request.putQueryParameter("PageSize", "50")
        request.putQueryParameter("StatusCode", "InProgress")
        val list = readListFromRequest<ScalingActivity>(locator, request, "ScalingActivities", "ScalingActivity")

        if (list.isEmpty()) {
            log.debug("no data in Scaling Activities:$scalingGroupId")
            return emptyMap()
        }

        return list.associateWith { workFor(locator, it) }
    }

    fun privateIPForInstanceId(locator: ResourceLocator, instanceId: String): String? {
        if (instanceId.startsWith("eci-")) {
            return EciTool.findDetail(locator, setOf(instanceId)).firstOrNull()?.intranetIp
        }
        return EcsTool.findDetail(locator, setOf(instanceId)).firstOrNull()?.intranetIp
    }

    // https://help.aliyun.com/zh/auto-scaling/developer-reference/api-completelifecycleaction?spm=a2c4g.11186623.help-menu-25855.d_5_0_0_14_5.6030736bH4npxq
    fun completeLifecycleAction(
        locator: ResourceLocator,
        action: LifecycleAction,
        result: LifecycleActionResult,
        clientToken: String? = null
    ): String? {
        val request = Helper.commonEssRequest()
        request.sysAction = "CompleteLifecycleAction"
        request.putQueryParameter("RegionId", locator.region)
        request.putQueryParameter("LifecycleHookId", action.hookId)
        request.putQueryParameter("LifecycleActionToken", action.token)
        request.putQueryParameter("LifecycleActionResult", result.name)
        clientToken?.let {
            request.putQueryParameter("ClientToken", it)
        }

        return Helper.executeCommonRequest(locator, request)
    }

    private fun workFor(locator: ResourceLocator, activity: ScalingActivity): List<LifecycleAction> {
//        https://help.aliyun.com/zh/auto-scaling/developer-reference/api-describelifecycleactions?spm=a2c4g.11186623.help-menu-25855.d_5_0_0_14_4.30cc143e5QjKMN&scm=20140722.H_202473._.OR_help-T_cn~zh-V_1
        val request = Helper.commonEssRequest()
        request.sysAction = "DescribeLifecycleActions"
        request.putQueryParameter("RegionId", locator.region)
        request.putQueryParameter("ScalingActivityId", activity.id)
        request.putQueryParameter("LifecycleActionStatus", "Pending")
        request.putQueryParameter("MaxResults", "50")

        return readListFromRequest<LifecycleAction>(locator, request, "LifecycleActions", "LifecycleAction")
    }
}
