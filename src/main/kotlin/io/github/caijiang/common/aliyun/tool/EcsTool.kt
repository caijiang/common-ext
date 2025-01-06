package io.github.caijiang.common.aliyun.tool

import com.aliyuncs.eci.model.v20180808.DescribeContainerGroupsRequest
import io.github.caijiang.common.aliyun.Helper
import io.github.caijiang.common.aliyun.ResourceLocator

/**
 * @author CJ
 */
object EcsTool {

    fun findDetail(locator: ResourceLocator, id: String) {
        val request = DescribeContainerGroupsRequest()
        request.zoneId = locator.region
        request.containerGroupIds = setOf(id).joinToString(",", prefix = "[", postfix = "]", transform = {
            "\"${it}\""
        })
        println(request)
        println(request.containerGroupIds)
        val response = Helper.commonDefaultAcsClientFrom(locator).getAcsResponse(request)

        println(response.containerGroups)

    }

}