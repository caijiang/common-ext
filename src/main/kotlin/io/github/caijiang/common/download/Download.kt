package io.github.caijiang.common.download

import org.springframework.http.ResponseEntity
import java.net.URLEncoder

/**
 * 以该文件名称作为下载,参考:https://www.rfc-editor.org/rfc/rfc5987.txt
 *
 */
fun ResponseEntity.BodyBuilder.downloadWithFileName(name: String): ResponseEntity.BodyBuilder {
    return header(
        "Content-Disposition",
        "attachment; filename* = UTF-8''" + URLEncoder.encode(
            name,
            "UTF-8"
        )
    )
}