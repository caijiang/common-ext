package io.github.caijiang.common.aliyun.tool

import io.github.caijiang.common.aliyun.AliyunTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * @author CJ
 */
@Disabled
class EcsToolTest {

    @Test
    fun findDetail() {
        EcsTool.findDetail(AliyunTest.fetchResourceLocator(), "eci-bp10lm8fxmyroczh18kc")
    }
}