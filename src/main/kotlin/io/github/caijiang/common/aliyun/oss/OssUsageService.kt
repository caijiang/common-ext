package io.github.caijiang.common.aliyun.oss

import java.io.InputStream

/**
 * @see OssProperties
 * @author CJ
 * @since 2.4.0
 */
interface OssUsageService {

    /**
     * 上传私有资源
     * @param resourcePath 资源路径
     * @throws NotImplementedError 如果尚未配置私有区域的配置
     * @throws java.io.IOException 上传过程，包括 oss方面
     */
    fun uploadPrivateResource(resourcePath: String, data: InputStream)

    /**
     * 上传公开资源
     * @param resourcePath 资源路径
     * @throws NotImplementedError 如果尚未配置公开区域的配置
     * @throws java.io.IOException 上传过程，包括 oss方面
     */
    fun uploadPublicResource(resourcePath: String, data: InputStream): String

    fun publicResourceUrl(resourcePath: String): String

    /**
     * @return 临时可以访问的私有资源地址
     * @throws java.io.IOException 上传过程，包括 oss方面
     */
    fun privateResourceTemporaryUrl(resourcePath: String): String

    /**
     * @see [uploadPrivateResource]
     * @see [privateResourceTemporaryUrl]
     */
    fun uploadPrivateResourceForUrl(resourcePath: String, data: InputStream): String {
        uploadPrivateResource(resourcePath, data)
        return privateResourceTemporaryUrl(resourcePath)
    }

}