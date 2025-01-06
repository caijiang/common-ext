package io.github.caijiang.common.aliyun.tool

import com.aliyuncs.ecs.model.v20140526.DescribeInstancesRequest
import com.aliyuncs.ecs.model.v20140526.DescribeInstancesResponse
import io.github.caijiang.common.aliyun.Helper
import io.github.caijiang.common.aliyun.ResourceLocator

/**
 * @author CJ
 */
object EcsTool {

    fun findDetail(locator: ResourceLocator, ids: Collection<String>): List<DescribeInstancesResponse.Instance> {
        val request = DescribeInstancesRequest()
        request.instanceIds = ids.joinToString(",", prefix = "[", postfix = "]", transform = {
            "\"${it}\""
        })
        val response = Helper.commonDefaultAcsClientFrom(locator).getAcsResponse(request)

        return response?.instances ?: emptyList()
    }
}