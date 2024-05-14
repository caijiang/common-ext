package io.github.caijiang.common.mysql

import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.StringUtils
import java.sql.ResultSet

/**
 * @author CJ
 */
@Suppress("unused")
object MysqlCharsetCorrect {
    private val log = LoggerFactory.getLogger(MysqlCharsetCorrect::class.java)

    data class TextColumnCharset(
        val tableName: String, val columns: Collection<String>,
        /**
         * 字符集
         * 比如 **`latin1`**,**`utf8`**,**`utf8mb4`**
         */
        val charset: String
    )

    /**
     * 自动纠正 mysql 字段的字符集
     */
    @JvmStatic
    fun mysqlTextCharsetCorrect(template: JdbcTemplate, target: Collection<TextColumnCharset>) {
        log.debug("准备纠正 mysql 文本字段的 charset...")
        val collations = target.map { it.charset }.distinct()
            .associateWith {
                val sql = "SHOW CHARACTER SET like ?"
                template.query<String>(sql, { rs: ResultSet ->
                    if (rs.next()) {
                        var index = 1
                        while (index <= rs.metaData.columnCount) {
                            val v1 = rs.metaData.getColumnName(index)
                            val v2 = rs.metaData.getColumnLabel(index)
                            if ((v1 != null && v1.contains("collation") || (v2 != null && v2.contains(
                                    "collation"
                                )))
                            ) {
                                return@query rs.getString(index)
                            }
                            index++
                        }
                        throw IllegalStateException("database has no collation field from $sql")
                    }
                    throw IllegalStateException("database has no charset like $it")
                }, it)
            }

        val sql = target.flatMap {
            charsetCorrect(
                template,
                it.tableName,
                it.columns,
                it.charset,
                collations[it.charset]!!
            )
        }

        log.debug("需要执行{}条 sql", sql.size)

        if (sql.isNotEmpty()) {
            log.info("准备批量执行{}", sql.size)
            try {
                template.batchUpdate(*sql.toTypedArray())
            } catch (e: Exception) {
                log.error("执行字符集异常", e)
                log.info("可以考虑手动执行以下脚本:")
                log.info("###########################")
                for (s in sql) {
                    log.info(s)
                }
            }
        }
    }


    private data class FieldDefine(
        val field: String, val type: String,
        val collation: String?, val nullable: Boolean,
        val defaultValue: String?,
        val comment: String?
    )

    private fun charsetCorrect(
        template: JdbcTemplate, tableName: String, columns: Collection<String>, charset: String,
        collation: String
    ): List<String> {
        val fieldDefines = try {
            template.query(
                "show full fields from $tableName"
            ) { rs: ResultSet, _: Int ->
                FieldDefine(
                    rs.getString("Field"),
                    rs.getString("Type"),
                    rs.getString("Collation"),
                    rs.getBoolean("Null"),
                    rs.getString("Default"),
                    rs.getString("Comment")
                )
            }
        } catch (e: DataAccessException) {
            log.info("处理${tableName}时，发生了错误，我们忽略了这个错误。", e)
            return emptyList()
        }
        return fieldDefines
            .filter {
                columns.any { x -> x.equals(it.field, ignoreCase = true) }
            }
            .filter {
                !collation.equals(it.collation, ignoreCase = true)
            }
            .map {
                val builder = StringBuilder("alter table `")
                builder.append(tableName)
                builder.append("` modify `").append(it.field).append("` ").append(it.type).append(" charset ")
                    .append(charset)
                if (it.defaultValue != null) {
                    builder.append(" default '").append(it.defaultValue).append("'")
                }
                if (it.nullable) {
                    builder.append(" null")
                } else {
                    builder.append(" not null")
                }
                if (StringUtils.hasLength(it.comment)) {
                    builder.append(" comment '").append(it.comment).append("'")
                }
                builder.toString()
            }
    }
}