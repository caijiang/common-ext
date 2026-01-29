package io.github.caijiang.common.k8s

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.client.KubernetesClient
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

    fun topOwner(input: HasMetadata, client: KubernetesClient): HasMetadata {
        // 自己的 pod 信息, 然后逐级寻找自己的 owner 信息
        var i = 0
        var currentResource: HasMetadata = input
        while (i++ < 10) {
            val ownerReferenceList = currentResource
                .metadata
                .ownerReferences
            if (ownerReferenceList.isNullOrEmpty()) {
                return currentResource
            }
            val target = ownerReferenceList.first()
            currentResource = client.genericKubernetesResources(target.apiVersion, target.kind)
                .inNamespace(currentResource.metadata.namespace)
                .withName(target.name)
                .get() ?: return currentResource
        }
        return currentResource
    }

}