package io.github.caijiang.common.job.worker

import io.github.caijiang.common.job.worker.test.JobWorkerApp
import io.github.caijiang.common.job.worker.test.JobWorkerAppWithSchedule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


private fun mockBeanWorks(jobTypeRunner: JobTypeRunner, scheduledJobService: ScheduleJobService) {
    clearInvocations(jobTypeRunner)

    val temporaryJob = object : TemporaryJob {
        override val type: String
            get() = "tt"
        override val parameters: Map<String, String>
            get() = mapOf("tt" to "temporary")
    }
    scheduledJobService.submitTemporaryJob(temporaryJob)

    Thread.sleep(10)
    verify(jobTypeRunner, times(1))
        .run(argThat {
            type == temporaryJob.type && parameters == temporaryJob.parameters
        })

    val persistentJob = object : PersistentJob {
        override val name: String
            get() = "name"
        override val type: String
            get() = "tt2"
        override val parameters: Map<String, String>
            get() = mapOf("tt" to "temporary2")
    }

    scheduledJobService.submitPersistentJob("* * * * *", persistentJob, springCronSeconds = "*")
    Thread.sleep(1000)
    verify(jobTypeRunner, times(1))
        .run(argThat {
            type == persistentJob.type && parameters == persistentJob.parameters
        })
}

/**
 * @author CJ
 */
@SpringBootTest(
    classes = [JobWorkerApp::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
class JobWorkerAutoConfigurationTest {

    @Test
    fun scheduleJobService(
        @Autowired jobTypeRunner: JobTypeRunner,
        @Autowired(required = false) scheduledJobService: ScheduleJobService?
    ) {
        assertThat(scheduledJobService).isNotNull

        mockBeanWorks(jobTypeRunner, scheduledJobService!!)

    }

}

@SpringBootTest(
    classes = [JobWorkerAppWithSchedule::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
class JobWorkerAutoConfigurationWithScheduleTest {

    @Test
    fun scheduleJobService(
        @Autowired jobTypeRunner: JobTypeRunner,
        @Autowired(required = false) scheduledJobService: ScheduleJobService?
    ) {
        assertThat(scheduledJobService).isNotNull

        mockBeanWorks(jobTypeRunner, scheduledJobService!!)

    }

}