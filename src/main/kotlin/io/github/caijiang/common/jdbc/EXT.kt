package io.github.caijiang.common.jdbc

import org.springframework.jdbc.core.JdbcTemplate


/**
 * @param collateName The name of the collation.
 * @see JdbcUtils.mysqlCollate
 */
fun JdbcTemplate.mysqlCollate(collateName: String, tables: Map<String, Set<String>>) {
    JdbcUtils.mysqlCollate(this, collateName, tables)
}