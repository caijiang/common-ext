package io.github.caijiang.common.job.scheduler

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.job.worker.PersistentJob
import io.github.caijiang.common.job.worker.SerializableJob
import io.github.caijiang.common.job.worker.TemporaryJob
import io.github.caijiang.common.k8s.KubernetesUtils
import java.util.*
import kotlin.time.Duration.Companion.days

/**
 * k8s支持，即 k8s Job.
 * CE_JOB_TYPE=
 * CE_JOB_ARG_`[NAME`]
 * @author CJ
 */
class KubernetesJobScheduler(
    private val client: KubernetesClient = KubernetesClientBuilder().build(),
) : Scheduler {

    private fun createPodTemplateSpec(
        env: String,
        hostname: String,
        job: SerializableJob,
        jobLabels: Map<String, String>
    ): PodTemplateSpec {
        val origins = client.pods().inNamespace(env)
            .withName(hostname).get().spec

        val newSpec = PodSpecBuilder(origins)
            .build()

        // 对环境动手
        newSpec.containers.forEach {
            buildEnvsFor(it, job)
        }
        // 规则
        newSpec.restartPolicy = "Never"
        // 移除所有探针
        newSpec.containers.forEach {
            removeAllProbe(it)
        }
        // 移除 node
        newSpec.nodeName = null
        return PodTemplateSpecBuilder()
            .withSpec(newSpec)
            .withNewMetadata()
            .withLabels<String, String>(
                jobLabels
            )
            .endMetadata()
            .build()
    }

    private fun removeAllProbe(container: Container) {
        container.readinessProbe = null
        container.startupProbe = null
        container.livenessProbe = null
    }

    private fun buildEnvsFor(container: Container, job: SerializableJob) {
        @Suppress("UselessCallOnNotNull")
        if (container.env?.isNullOrEmpty() == true) {
            container.env = createEnvList(job)
        } else {
            container.env = container.env.filter {
                it.name?.startsWith("CE_JOB_") != true
            } + createEnvList(job)
        }
    }

    private fun createEnvList(job: SerializableJob): List<EnvVar> {
        return job.parameters.mapKeys {
            "CE_JOB_ARG_${it.key}"
        }.map {
            EnvVarBuilder()
                .withName(it.key)
                .withValue(it.value)
                .build()
        } + listOf(
            EnvVarBuilder()
                .withName("CE_JOB_TYPE")
                .withValue(job.type)
                .build()
        )
    }

    private fun jobLabels(env: String, hostname: String, job: SerializableJob): Map<String, String> {
        // 获取部署物 deployment-kind, deployment-id
        val deploymentLabels = try {
            val a = client.pods().inNamespace(env)
                .withName(hostname)
                .get()
            val root = KubernetesUtils.topOwner(a, client)
            mapOf(
                "job.common-ext.caijiang.github.io/deployment-kind" to root.kind,
                "job.common-ext.caijiang.github.io/deployment-id" to root.metadata.name
            )
        } catch (e: Exception) {
            log.error("获取部署信息时", e)
            mapOf()
        }
        return mapOf(
            "job.common-ext.caijiang.github.io/from-env" to env,
            "job.common-ext.caijiang.github.io/from-hostname" to hostname,
            "job.common-ext.caijiang.github.io/type" to job.type,
        ) + deploymentLabels
    }

    override fun submitTemporaryJob(env: String, hostname: String, job: TemporaryJob) {
        val jobLabels = jobLabels(env, hostname, job)
        val k8sJob = JobBuilder()
            .withNewMetadata()
            .withNamespace(env)
            .withGenerateName("ce-temp-job-${job.type}-")
            .withLabels<String, String>(jobLabels)
            .endMetadata()
            .withNewSpec()
            .withTtlSecondsAfterFinished(1.days.inWholeSeconds.toInt())
            .withTemplate(createPodTemplateSpec(env, hostname, job, jobLabels))
            .endSpec()
            .build()

        log.debug("准备创建 k8s-job:{}", k8sJob)

        client.batch().v1().jobs()
            .resource(k8sJob)
            .create()
    }

    override fun submitPersistentJob(
        env: String,
        hostname: String,
        cron: String,
        job: PersistentJob,
        timezone: TimeZone
    ) {
        val jobLabels = jobLabels(env, hostname, job)

        val k8sJob = CronJobBuilder()
            .withNewMetadata()
            .withNamespace(env)
            .withName("ce-job-" + job.name)
            .withLabels<String, String>(jobLabels)
            .endMetadata()
            .withNewSpec()
            // "Forbid"：禁止并发运行，如果上一次运行尚未完成则跳过下一次运行；
            .withConcurrencyPolicy("Forbid")
            .withSchedule(cron)
            .withTimeZone(timezone.toZoneId().id)
            .withNewJobTemplate()
            .withNewSpec()
            .withTemplate(createPodTemplateSpec(env, hostname, job, jobLabels))
            .endSpec()
            .endJobTemplate()
            .endSpec()
            .build()

        log.debug("准备创建 k8s-cron-job:{}", k8sJob)

        client.batch().v1().cronjobs()
            .resource(k8sJob)
            .createOr {
                it.update()
            }
    }

    override fun cleanPersistentJob(env: String, hostname: String, jobName: String) {
        client.batch().v1().cronjobs()
            .resource(
                CronJobBuilder()
                    .withNewMetadata()
                    .withNamespace(env)
                    .withName("ce-job-$jobName")
                    .endMetadata()
                    .build()
            )
            .delete()
    }

}