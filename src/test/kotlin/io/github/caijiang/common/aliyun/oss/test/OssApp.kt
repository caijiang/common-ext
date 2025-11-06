package io.github.caijiang.common.aliyun.oss.test

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration

/**
 * @author CJ
 */
@SpringBootApplication(exclude = [JpaRepositoriesAutoConfiguration::class])
open class OssApp