package io.github.caijiang.common.job.worker

import java.util.*

/**
 * 调度任务服务
 * @since 2.6.0
 * @author CJ
 */
interface ScheduleJobService {

    /**
     * 调度一个可序列化任务
     */
    fun submitTemporaryJob(job: TemporaryJob)

    /**
     * 定时调度任务
     * @param cron [https://zh.wikipedia.org/wiki/Cron](https://zh.wikipedia.org/wiki/Cron)
     * @param springCronSeconds spring cron 会比标准 cron 多一位;绝大多数情况忽略这个字段即可
     */
    fun submitPersistentJob(
        cron: String,
        job: PersistentJob,
        timezone: TimeZone = TimeZone.getDefault(),
        springCronSeconds: String = "0"
    )

}