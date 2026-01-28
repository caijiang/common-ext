package io.github.caijiang.common.k8s

import java.io.File

/**
 * @since 2.6.0
 * @author CJ
 */
object KubernetesUtils {
    /**
     * @return 当前 k8s namespace; null 表示并不是在 k8s集群中
     */
    @JvmStatic
    fun currentNamespace(): String? {
        val env: String? = System.getenv("kubenamespace") ?: System.getProperty("kubenamespace")
        if (env?.isNotBlank() == true) {
            return env
        }
        try {
            return File("/var/run/secrets/kubernetes.io/serviceaccount/namespace")
                .readText()
        } catch (ignored: Exception) {
            return null
        }
    }
}