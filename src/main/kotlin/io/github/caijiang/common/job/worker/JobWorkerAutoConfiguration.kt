package io.github.caijiang.common.job.worker

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.job.scheduler.Scheduler
import io.github.caijiang.common.job.worker.bean.SchedulerScheduleJobService
import io.github.caijiang.common.job.worker.bean.TaskSchedulerScheduleJobService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.SchedulingConfiguration

/**
 * @author CJ
 */
@AutoConfiguration
@ConditionalOnBean(JobTypeRunner::class)
@AutoConfigureAfter(SchedulingConfiguration::class)
@EnableConfigurationProperties(JobWorkerConfiguration::class)
open class JobWorkerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun scheduleJobService(
        /**
         * worker 模式一般不会为空
         */
        jobTypeRunner: JobTypeRunner,
        configuration: JobWorkerConfiguration,
        /**
         * 本地调度器，大概率不会有
         */
        @Autowired(required = false) scheduler: Scheduler? = null,
//        /**
//         * spring 并且启用了 Schedule 功能
//         */
//        @Autowired(required = false) taskRegister: ScheduledTaskRegistrar? = null,
        @Autowired(required = false) taskScheduler: TaskScheduler? = null
    ): ScheduleJobService {
        // 1, 有没有本地调度器
        if (scheduler != null) {
            log.info("存在内置 Scheduler 使用内置调度")
            return SchedulerScheduleJobService(
                jobTypeRunner,
                scheduler,
            )
        }
        // 2，查看是否在 k8s 环境,在的话 关注一下有没有调度 api;
        val s = configuration.readSchedulerServiceUrl()?.let {
            RemoteScheduler(it)
        }
        // SchedulingConfigurer.class, TaskScheduler.class, ScheduledExecutorService.class
        if (s != null) {
            log.info("存在集群调度服务 使用集群调度:{}", configuration.readSchedulerServiceUrl())
            return SchedulerScheduleJobService(jobTypeRunner, s)
        }

        if (taskScheduler != null) {
            return TaskSchedulerScheduleJobService(jobTypeRunner, taskScheduler)
        }
        return TaskSchedulerScheduleJobService(jobTypeRunner)
//        // 3, 使用 spring
//        if (taskRegister != null) {
//            log.info("spring 环境，使用 spring 内置调度器")
//            return ScheduledTaskRegistrarScheduleJobService(taskRegister, jobTypeRunner!!)
//        }
//        // 4, 使用 本地线程池
//        return LocalScheduleJobService(jobTypeRunner!!)
    }
}