package io.github.caijiang.common.download

import org.springframework.http.ResponseEntity
import java.net.URLEncoder

object ResponseEntityUtils {
    @JvmStatic
    fun downloadWithFileName(input: ResponseEntity.BodyBuilder, name: String): ResponseEntity.BodyBuilder {
        return input.header(
            "Content-Disposition",
            "attachment; filename* = UTF-8''" + URLEncoder.encode(
                name,
                "UTF-8"
            )
        )
    }
}

/**
 * 以该文件名称作为下载,参考:https://www.rfc-editor.org/rfc/rfc5987.txt
 * @since 2.3.0
 */
fun ResponseEntity.BodyBuilder.downloadWithFileName(name: String): ResponseEntity.BodyBuilder {
    return ResponseEntityUtils.downloadWithFileName(this, name)
}