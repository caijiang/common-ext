package io.github.caijiang.common.job.worker

import java.util.*

/**
 * 调度任务服务
 *
 * **需要注意的是，调度任务服务只是负责调度的！！**
 *
 * 经过调度后，具体任务将被安排在[JobTypeRunner]中负责执行。如果执行器所处环境为该任务而临时建立的，那么在任务[JobTypeRunner.run]完成后会立刻[JobTypeRunner.quitApplication]并且尝试终止整个环境。
 * 非集群环境则会尝试当前虚拟机中调用[JobTypeRunner]。
 *
 * @since 2.6.0
 * @author CJ
 */
interface ScheduleJobService {

    /**
     * 马上调度一个一次性临时任务
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

    /**
     * 清除掉一个定时的调度任务
     * @param jobName [PersistentJob.name]
     * @since 2.6.1
     */
    fun cleanPersistentJob(jobName: String)

}