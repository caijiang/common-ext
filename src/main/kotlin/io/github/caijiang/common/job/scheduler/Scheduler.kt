package io.github.caijiang.common.job.scheduler

import io.github.caijiang.common.job.worker.PersistentJob
import io.github.caijiang.common.job.worker.TemporaryJob
import java.util.*


/**
 * 任务调度器
 * @author CJ
 */
interface Scheduler {

    /**
     * 调度一个可序列化任务
     * @param env 机器所在环境，缺省为`default`
     */
    fun submitTemporaryJob(env: String, hostname: String, job: TemporaryJob)

    /**
     * 定时调度任务
     * @param env 机器所在环境，缺省为`default`
     */
    fun submitPersistentJob(env: String, hostname: String, cron: String, job: PersistentJob, timezone: TimeZone)

    /**
     * 清理定时调度任务
     * @param env 机器所在环境，缺省为`default`
     */
    fun cleanPersistentJob(env: String, hostname: String, jobName: String)
}