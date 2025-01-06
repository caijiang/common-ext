package io.github.caijiang.common.aliyun

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


/**
 * @author CJ
 */
@Disabled
class ScalingActivityLifecycleCallbackTest {

    @Test
    fun queryLifecycleActions() {
        val locator = AliyunTest.fetchResourceLocator()

        println(locator)
        ScalingActivityLifecycleCallback.queryLifecycleActions(
            locator, "asg-bp11zgc73nsd69d4if0j"
        )
    }
}