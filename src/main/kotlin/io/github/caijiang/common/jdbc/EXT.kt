package io.github.caijiang.common.jdbc

import io.github.caijiang.common.mysql.MysqlCharsetCorrect
import org.springframework.jdbc.core.JdbcTemplate


/**
 * @param collateName The name of the collation.
 * @see JdbcUtils.mysqlCollate
 */
@Suppress("DEPRECATION")
@Deprecated("下一个", ReplaceWith("mysqlCollateCorrect(target)"))
fun JdbcTemplate.mysqlCollate(collateName: String, tables: Map<String, Set<String>>) {
    JdbcUtils.mysqlCollate(this, collateName, tables)
}

/**
 * 自动纠正 mysql 字段的字符集
 * @since 1.1.0
 */
fun JdbcTemplate.mysqlCollateCorrect(target: Collection<MysqlCharsetCorrect.TextColumnCharset>) {
    MysqlCharsetCorrect.mysqlTextCharsetCorrect(this, target)
}