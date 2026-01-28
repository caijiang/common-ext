package io.github.caijiang.common.job.worker

/**
 * @author CJ
 */
interface JobTypeRunner {
    /**
     * 在当前线程执行这个任务
     */
    fun run(job: SerializableJob)
}