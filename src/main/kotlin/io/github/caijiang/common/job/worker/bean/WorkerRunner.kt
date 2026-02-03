package io.github.caijiang.common.job.worker.bean

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.job.worker.JobTypeRunner
import io.github.caijiang.common.job.worker.PersistentJob
import io.github.caijiang.common.job.worker.SerializableJob
import kotlin.system.exitProcess

/**
 * @author CJ
 */
internal object WorkerRunner {
    fun executeCurrentJob(runner: JobTypeRunner) {
        // 看看当前有没有任务
        val type: String? = System.getenv("CE_JOB_TYPE")
        log.debug("检查当前是否为任务模式:{}", type)
        if (type?.isNotBlank() == true) {
            val ps = System.getenv().filterKeys {
                it?.startsWith("CE_JOB_ARG_") == true
            }.mapKeys {
                it.key.removePrefix("CE_JOB_ARG_")
            }.filterValues { it != null }
            try {
                log.info("准备执行任务:{},参数:{}", type, ps)
                runner.run(object : SerializableJob {
                    override val type: String
                        get() = type
                    override val parameters: Map<String, String>
                        get() = ps
                })
                log.info("任务:{},参数:{}完成,即将使用退出代码 0 结束 进程", type, ps)
                if (try {
                        runner.quitApplication()
                    } catch (e: Exception) {
                        false
                    }
                )
                    exitProcess(0)
                else
                    Runtime.getRuntime().halt(0)
            } catch (e: Throwable) {
                log.error("执行任务:{},参数:{}报错,即将使用退出代码 1 结束进程", type, ps, e)
                if (try {
                        runner.quitApplication()
                    } catch (e: Exception) {
                        false
                    }
                )
                    exitProcess(1)
                else
                    Runtime.getRuntime().halt(1)
            }
        }
    }

    private val rfc1123 = Regex("^[a-z0-9]([-a-z0-9]*[a-z0-9])?$")
    private val envName = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
    fun valid(job: SerializableJob) {
        if (!rfc1123.matches(job.type)) {
            throw IllegalArgumentException("job.type:${job.type} is not valid")
        }
        if (job.type.length > 43) {
            throw IllegalArgumentException("job.type:${job.type} too large")
        }
        if (job.type.isEmpty()) {
            throw IllegalArgumentException("job.type:${job.type} can not be empty")
        }
        if (job.parameters.keys.any {
                !envName.matches(it)
            }) {
            throw IllegalArgumentException("job.parameters:${job.parameters} is not valid")
        }
        if (job is PersistentJob) {
            validJobName(job.name)
        }
    }

    fun validJobName(jobName: String) {
        if (!rfc1123.matches(jobName))
            throw IllegalArgumentException("job.name:${jobName} is not valid")
        if (jobName.length > 53) {
            throw IllegalArgumentException("job.name:${jobName} too large")
        }
        if (jobName.isEmpty()) {
            throw IllegalArgumentException("job.name:${jobName} can not be empty")
        }
    }
}