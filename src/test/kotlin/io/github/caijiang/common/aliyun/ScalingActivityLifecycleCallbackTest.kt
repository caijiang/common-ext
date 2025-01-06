package io.github.caijiang.common.aliyun

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml


/**
 * @author CJ
 */
@Disabled
class ScalingActivityLifecycleCallbackTest {

    private fun fetchResourceLocator(): ResourceLocator {
        val data = Yaml().load<Map<String, String>>(ClassPathResource("local-aliyun-data.yaml").inputStream)
        return ResourceLocator(
            accessKeyId = data["accessKeyId"].toString(),
            accessKeySecret = data["accessKeySecret"].toString(),
            region = data["region"].toString()
        )
    }

    @Test
    fun queryLifecycleActions() {
        val locator = fetchResourceLocator()

        println(locator)
        ScalingActivityLifecycleCallback.queryLifecycleActions(
            locator, "asg-bp11zgc73nsd69d4if0j"
        )
    }
}