package io.github.caijiang.common.mysql

import io.github.caijiang.common.mysql.MySqlParser.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.Interval
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.ClassUtils
import java.io.InputStream
import java.util.*

/**
 * 负责执行 ddl
 * @author CJ
 */
@Suppress("SqlSourceToSinkFlow")
object Ddl {
    private val log = LoggerFactory.getLogger(Ddl::class.java)

    enum class ExecutePolicy {
        /**
         * 执行诸如 insert,delete,update 等语句；缺省是忽略 dml
         */
        ExecuteDML,

    }

    /**
     * 执行 script 脚本
     * dml 是否执行依赖输入策略[policy]
     * 危险的 ddl 是不可以被执行的,比如 drop table
     *
     * 执行 `create table` 时依据以下策略:
     * 1. 如果目标表不存在直接执行
     * 1. 如果目标表缺少期望的字段，则执行 `add`
     * 1. 如果当前表存在目标表不存在的字段 仅仅给予警告
     * 1. 如果字段都存在，仅支持同类型扩容 类型完全兼容的只会给出警告
     *
     * 其他 ddl 暂不支持
     *
     * @see ExecutePolicy
     * @since 1.1.0
     */
    @JvmStatic
    fun executeScriptResource(template: JdbcTemplate, resource: Resource, vararg policy: ExecutePolicy) {
        if (!ClassUtils.isPresent("org.antlr.v4.runtime.CharStream", null)) {
            throw IllegalStateException("antlr v4 runtime can not found in classpath;visit https://mvnrepository.com/artifact/org.antlr/antlr4-runtime to find a fav.")
        }
        // 读取所有的表以及 表定义
        try {
            val tables = template.queryMysqlTableNames()

            val allFieldDefines = tables.associateWith { FieldDefine.fromTemplate(template, it) }

            resource.inputStream.use { inputStream ->
                val charStream = charStream(inputStream)
                val parser = MySqlParser(CommonTokenStream(MySqlLexer(charStream)))
                val root = parser.root()
                root.sqlStatements()
                    .sqlStatement()
                    .forEach {
                        val ddl = it.ddlStatement()
                        if (ddl != null && !ddl.isEmpty) {
                            val createTable = ddl.createTable()
                            if (createTable != null && !createTable.isEmpty) {
                                executeCreateTable(template, createTable, charStream, allFieldDefines)
                            }
                            val dropTable = ddl.dropTable()
                            if (dropTable != null && !dropTable.isEmpty) {
                                log.warn("drop table will not execute:({})", dropTable.toOriginalText(charStream))
                            }
                        }

                        val dml = it.dmlStatement()
                        if (dml != null && !dml.isEmpty) {
                            if (policy.contains(ExecutePolicy.ExecuteDML)) {
                                log.info("executing dml: ({})", dml.toOriginalText(charStream))
                                template.execute(dml.toOriginalText(charStream))
                            } else {
                                log.warn(
                                    "dml statement:({}) will not execute until ExecutePolicy.ExecuteDML",
                                    dml.toOriginalText(charStream)
                                )
                            }
                        }

                    }

            }
        } catch (e: Exception) {
            log.warn("apply ddl error", e)
        }

    }

    // 如果当前不存在 那就执行
    // 如果当前存在 就逐个检查表字段:
//                            存在语句中不存在的字段 给予警告
//                            存在语句中不存在字段 就 alter 实施新增
//                            存在语句中字段定义不一致的 就 alter 实施修改
    private fun executeCreateTable(
        template: JdbcTemplate,
        context: CreateTableContext,
        charStream: CharStream,
        allFieldDefines: Map<String, List<FieldDefine>>
    ) {
        val nameContext = context.children.filterIsInstance<TableNameContext>()
            .first()
        val name = nameContext
            .toPureTableName()

        // 存在了
        if (allFieldDefines.keys.any { it.equals(name, true) }) {
            val currentFields = allFieldDefines.entries.first { it.key.equals(name, true) }.value
            val sqlFields = FieldDefine.fromContext(context)

            log.debug("current fields: {}", currentFields)
            log.debug("SQL fields: {}", sqlFields)

            // 存在语句中不存在的字段 给予警告
            val unused = currentFields.filter { current ->
                sqlFields.none { it.field.equals(current.field, true) }
            }.map { it.field }

            if (unused.isNotEmpty()) {
                log.warn("table:{} has fields to drop:{}", name, unused)
            }

            // 存在语句中不存在字段 就 alter 实施新增
            val newInSql = sqlFields.filter { s ->
                currentFields.none { it.field.equals(s.field, true) }
            }
            if (newInSql.isNotEmpty()) {
                context.children.filterIsInstance<CreateDefinitionsContext>()
                    .first()
                    .createDefinition()
                    .filterIsInstance<ColumnDeclarationContext>()
                    .filter { inputField ->
                        val fieldName = inputField.fullColumnName().uid().simpleId().text
                        newInSql.any {
                            it.field.equals(fieldName, true)
                        }
                    }.forEach {
                        executeColumnChange(nameContext, charStream, "add", it, template)
                    }
            }
            // 有都存在的 比较一下 是否需要更新
            val both = sqlFields.map {
                it.field
            }.filter { x ->
                currentFields.any {
                    it.field.equals(x, true)
                }
            }

            if (both.isNotEmpty()) {
                log.debug("table:{} has fields both:{}", name, both)
                both.forEach { fieldName ->
                    val sql = sqlFields.first {
                        it.field.equals(fieldName, true)
                    }
                    val current = currentFields.first {
                        it.field.equals(fieldName, true)
                    }
                    if (upgradeAble(current, sql)) {
                        context.children.filterIsInstance<CreateDefinitionsContext>()
                            .first()
                            .createDefinition()
                            .filterIsInstance<ColumnDeclarationContext>()
                            .filter { inputField ->
                                val fn = inputField.fullColumnName().uid().simpleId().text
                                fieldName.equals(fn, true)
                            }.forEach {
                                executeColumnChange(nameContext, charStream, "modify", it, template)
                            }
                    }
                }
            }


        } else {
            template.execute(context.toOriginalText(charStream))
        }
    }

    private fun executeColumnChange(
        nameContext: TableNameContext,
        charStream: CharStream,
        action: String,
        it: ColumnDeclarationContext,
        template: JdbcTemplate
    ) {
        val sql = "alter table ${nameContext.toOriginalText(charStream)} $action  column ${
            it.fullColumnName().toOriginalText(charStream)
        } ${it.columnDefinition().toOriginalText(charStream)}"
        log.info("executing column change sql:{}", sql)
        template.execute(sql)
    }

    private fun upgradeAble(current: FieldDefine, last: FieldDefine): Boolean {
        if (!Objects.equals(current.defaultValue, last.defaultValue)) {
            return true
        }
        if (!Objects.equals(current.comment, last.comment)) {
            return true
        }
        // Numeric
        if (current.isNumeric1() && last.isNumeric1()) {
            return last.numeric1Level()!! > current.numeric1Level()!!
        }

        if (current.isNumeric2() && last.isNumeric2()) {
            return last.numeric2Level()!! > current.numeric2Level()!!
        }

        val cb = current.toBinLevel()
        val lb = last.toBinLevel()
        if (cb != null && lb != null) {
            return lb > cb
        }

        val ct = current.toTextLevel()
        val lt = last.toTextLevel()
        if (ct != null && lt != null) {
            return lt > ct
        }

        log.debug("can not decide which one is better in {} {}", current.type, last.type)
        return false
    }

    private fun charStream(inputStream: InputStream): CharStream {
        if (ClassUtils.isPresent("org.antlr.v4.runtime.CharStream", null)) {
            return CharStreams.fromStream(inputStream)
        }
        @Suppress("DEPRECATION")
        return ANTLRInputStream(inputStream)
    }


}

internal fun JdbcTemplate.queryMysqlTableNames(): List<String> {
    return query(
        "show tables"
    ) { rs, _ -> rs.getString(1) }
}

private fun TableNameContext.toPureTableName(): String {
    return fullId().uid().last().toPureIdName()
}

private fun UidContext.toPureIdName(): String {
    val text = simpleId().text
    if (text.startsWith('`') && text.endsWith('`')) {
        return text.substring(1, text.length - 1)
    }
    return simpleId().text
}

/**
 * @return 原始字符串
 */
private fun ParserRuleContext.toOriginalText(charStream: CharStream): String {
    return charStream.getText(
        Interval(
            start.startIndex, stop.stopIndex
        )
    )
}
