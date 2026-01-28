package io.github.caijiang.common.job.worker

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author CJ
 */
@ConfigurationProperties(prefix = "common.job")
data class JobWorkerConfiguration(
    /**
     * 调度服务的url
     */
    val schedulerServiceUrl: String? = null,
) {
    /**
     * 读取这个
     */
    fun readSchedulerServiceUrl(): String? {
        if (schedulerServiceUrl.isNullOrBlank()) {
            return System.getenv("JOB_SCHEDULER_SERVICE_URL")
        }
        return schedulerServiceUrl
    }
}
