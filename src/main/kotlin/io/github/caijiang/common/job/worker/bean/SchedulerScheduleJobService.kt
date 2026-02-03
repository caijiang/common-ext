package io.github.caijiang.common.job.worker.bean

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.job.scheduler.Scheduler
import io.github.caijiang.common.job.worker.JobTypeRunner
import io.github.caijiang.common.job.worker.PersistentJob
import io.github.caijiang.common.job.worker.ScheduleJobService
import io.github.caijiang.common.job.worker.TemporaryJob
import io.github.caijiang.common.k8s.KubernetesUtils
import java.util.*

class SchedulerScheduleJobService(
    runner: JobTypeRunner,
    private val scheduler: Scheduler,
    private val env: String = KubernetesUtils.currentNamespace() ?: "default",
    private val hostname: String = System.getenv("HOSTNAME") ?: "localhost",
) :
    ScheduleJobService {
    init {
        WorkerRunner.executeCurrentJob(runner)
    }

    override fun submitTemporaryJob(job: TemporaryJob) {
        WorkerRunner.valid(job)
        log.debug("向 Scheduler 发起 temporary 调度请求:当前设备:{},类型:{}", hostname, job.type)
        scheduler.submitTemporaryJob(env, hostname, job)
    }

    override fun submitPersistentJob(cron: String, job: PersistentJob, timezone: TimeZone, springCronSeconds: String) {
        WorkerRunner.valid(job)
        log.debug("向 Scheduler 发起 persistent 调度请求:当前设备:{},类型:{},名称:{}", hostname, job.type, job.name)
        scheduler.submitPersistentJob(env, hostname, cron, job, timezone)
    }

    override fun cleanPersistentJob(jobName: String) {
        WorkerRunner.validJobName(jobName)
        scheduler.cleanPersistentJob(env, hostname, jobName)
    }

}
