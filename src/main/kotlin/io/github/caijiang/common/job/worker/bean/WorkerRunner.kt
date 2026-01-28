package io.github.caijiang.common.job.worker.bean

import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.job.worker.JobTypeRunner
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
                log.info("任务:{},参数:{}完成,即将使用退出代码 0 结束进程", type, ps)
                exitProcess(0)
            } catch (e: Throwable) {
                log.error("执行任务:{},参数:{}报错,即将使用退出代码 1 结束进程", type, ps, e)
                exitProcess(1)
            }
        }
    }
}