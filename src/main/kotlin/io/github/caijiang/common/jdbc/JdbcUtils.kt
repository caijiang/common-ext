package io.github.caijiang.common.jdbc

import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate

object JdbcUtils {
    private val log = LoggerFactory.getLogger(JdbcUtils::class.java)

    private data class MysqlColumnDefine(
        val name: String,
        val type: String,
        val nullable: Boolean,
        val defaultToString: String?,
        val comment: String?,
        val collate: String?
    ) {
        override fun toString(): String {
            val ns = if (nullable) "not null" else "null"
            val ds = if (defaultToString == null) "" else "default $defaultToString"
            val cs = if (comment == null) "" else "comment '$comment'"
            val ca = if (collate == null) "" else "COLLATE $collate"
            return "$name $type $ns $ds $cs $ca"
        }
    }

    /**
     * 如果运行在 mysql 电话就调整一下 collate
     * @param collateName 目标 collation name
     * @param tables 目标表，以及其字段
     */
    @JvmStatic
    fun mysqlCollate(template: JdbcTemplate, collateName: String, tables: Map<String, Set<String>>) {
        if (template.execute(ConnectionCallback { con -> con.metaData.databaseProductName.equals("mysql", true) })!!) {
            tables.forEach { (t, u) ->
//            query("show full columns from $t") { rs ->
//                (1).rangeTo(rs.metaData.columnCount).forEach {
//                    println(rs.metaData.getColumnLabel(it))
//                    println(rs.metaData.getColumnType(it))
//                    println(rs.getString(it))
//                }
//            }
                val cs = template.query(
                    "show full columns from $t"
                ) { rs, _ ->
                    val d = rs.getObject("Default")
                    val name = rs.getString("Field")
                    name to MysqlColumnDefine(
                        name,
                        rs.getString("Type"),
                        !"no".equals(rs.getString("Null"), false),
                        if (d == null) null else if (d is String) "'${d}'" else null,
                        rs.getString("Comment"),
                        rs.getString("Collation")
                    )
                }
                val toChange = cs.filter { p ->
                    u.any { it.equals(p.first, true) }
                }.filter {
                    !collateName.equals(it.second.collate, true)
                }.map {
                    it.second.copy(collate = collateName)
                }

                if (toChange.isNotEmpty()) {
                    val sql = "alter table $t ${toChange.joinToString(",") { "modify column $it" }}"
                    log.info("prepare to execute sql: $sql")
                    template.update(sql)
                }

            }
        }
    }
}