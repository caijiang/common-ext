package io.github.caijiang.common.aliyun.tool

import com.aliyuncs.eci.model.v20180808.DescribeContainerGroupsRequest
import com.aliyuncs.eci.model.v20180808.DescribeContainerGroupsResponse
import io.github.caijiang.common.aliyun.Helper
import io.github.caijiang.common.aliyun.ResourceLocator

/**
 * @author CJ
 */
object EciTool {

    /**
     * @param ids eci 容器组 id
     */
    fun findDetail(
        locator: ResourceLocator,
        ids: Collection<String>
    ): List<DescribeContainerGroupsResponse.ContainerGroup> {
        val request = DescribeContainerGroupsRequest()
        request.containerGroupIds = ids.joinToString(",", prefix = "[", postfix = "]", transform = {
            "\"${it}\""
        })
        val response = Helper.commonDefaultAcsClientFrom(locator).getAcsResponse(request)

        return response?.containerGroups ?: emptyList()

    }

}