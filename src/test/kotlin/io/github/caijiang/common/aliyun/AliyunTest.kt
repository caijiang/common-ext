package io.github.caijiang.common.aliyun

import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml

/**
 * @author CJ
 */
object AliyunTest {

    fun fetchResourceLocator(): ResourceLocator {
        val data = Yaml().load<Map<String, String>>(ClassPathResource("local-aliyun-data.yaml").inputStream)
        return ResourceLocator(
            accessKeyId = data["accessKeyId"].toString(),
            accessKeySecret = data["accessKeySecret"].toString(),
            region = data["region"].toString()
        )
    }

}