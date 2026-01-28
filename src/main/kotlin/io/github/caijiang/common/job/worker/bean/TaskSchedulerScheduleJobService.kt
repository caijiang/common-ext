package io.github.caijiang.common.job.worker.bean

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.job.worker.JobTypeRunner
import io.github.caijiang.common.job.worker.PersistentJob
import io.github.caijiang.common.job.worker.ScheduleJobService
import io.github.caijiang.common.job.worker.TemporaryJob
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import java.util.*

/**
 * @author CJ
 */
class TaskSchedulerScheduleJobService(
    private val runner: JobTypeRunner,
    private val taskScheduler: TaskScheduler = ThreadPoolTaskScheduler().apply {
        initialize()
    },
) : ScheduleJobService {
    init {
        WorkerRunner.executeCurrentJob(runner)
    }

    override fun submitTemporaryJob(job: TemporaryJob) {
        log.debug("向 TaskScheduler 发起 temporary 调度请求:类型:{}", job.type)
        taskScheduler.schedule(
            {
                log.debug("TaskScheduler 执行 temporary 调度请求:类型:{}", job.type)
                runner.run(job)
            },
            Date()
        )
    }

    override fun submitPersistentJob(cron: String, job: PersistentJob, timezone: TimeZone, springCronSeconds: String) {
        log.debug("向 TaskScheduler 发起 persistent 调度请求:类型:{},名称:{}", job.type, job.name)
        taskScheduler.schedule({
            log.debug("TaskScheduler 执行 persistent 调度请求:类型:{},名称:{}", job.type, job.name)
            runner.run(job)
        }, CronTrigger("$springCronSeconds $cron", TimeZone.getDefault()))
    }

}