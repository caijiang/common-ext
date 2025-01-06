package io.github.caijiang.common.aliyun.tool

import io.github.caijiang.common.aliyun.AliyunTest
import io.github.caijiang.common.aliyun.ScalingActivityLifecycleCallback
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * @author CJ
 */
@Disabled
class EciToolTest {

    @Test
    fun findDetail() {
        val rs = EciTool.findDetail(AliyunTest.fetchResourceLocator(), setOf("eci-bp10lm8fxmyroczh18kc"))
        println(rs.map { it.intranetIp })

        val findDetail = EcsTool.findDetail(AliyunTest.fetchResourceLocator(), setOf("i-bp14bz84vz2kgzob5uhr"))
        println(
            findDetail
                .map { it.vpcAttributes.privateIpAddress.first() })

        println(
            ScalingActivityLifecycleCallback.privateIPForInstanceId(
                AliyunTest.fetchResourceLocator(),
                "eci-bp10lm8fxmyroczh18kc"
            )
        )

        println(
            ScalingActivityLifecycleCallback.privateIPForInstanceId(
                AliyunTest.fetchResourceLocator(),
                "i-bp14bz84vz2kgzob5uhr"
            )
        )
    }
}