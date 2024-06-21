package io.github.caijiang.common.mysql

import io.github.caijiang.common.mysql.MySqlParser.CreateDefinitionsContext
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet

internal data class FieldDefine(
    val field: String, val type: String,
    val collation: String?, val nullable: Boolean,
    val defaultValue: String?,
    val comment: String?
) {
    companion object {
        fun fromContext(context: MySqlParser.CreateTableContext): List<FieldDefine> {
            return context.children.filterIsInstance<CreateDefinitionsContext>()
                .first()
                // 一共有 IndexDeclarationContext ConstraintDeclarationContext ColumnDeclarationContext
                // 目前先处理  ColumnDeclarationContext
                .createDefinition()
                .filterIsInstance<MySqlParser.ColumnDeclarationContext>()
                .map {
                    val field = it.fullColumnName().uid().simpleId().text
                    val type = it.columnDefinition().dataType().text
                    val nullable = it.columnDefinition().columnConstraint()
                        .filterIsInstance<MySqlParser.NullColumnConstraintContext>()
                        .map { nn ->
                            nn.nullNotnull().text.equals("null", true)
                        }
                        .firstOrNull() ?: true

                    val defaultValue = it.columnDefinition().columnConstraint()
                        .filterIsInstance<MySqlParser.DefaultColumnConstraintContext>()
                        .map { d ->
                            val text = d.defaultValue().constant().text
                            if (text != null && text.startsWith("\'") && text.endsWith("\'")) {
                                text.substring(1, text.length - 1)
                            } else
                                text
                        }
                        .firstOrNull()

                    val comment = it.columnDefinition().columnConstraint()
                        .filterIsInstance<MySqlParser.CommentColumnConstraintContext>()
                        .map { d ->
                            val text = d.STRING_LITERAL().text
                            if (text != null && text.startsWith("\'") && text.endsWith("\'")) {
                                text.substring(1, text.length - 1)
                            } else
                                text
                        }
                        .firstOrNull()

                    FieldDefine(
                        field, type, null, nullable, defaultValue, comment
                    )
                }
        }

        internal fun fromTemplate(template: JdbcTemplate, tableName: String): List<FieldDefine> {
            @Suppress("SqlSourceToSinkFlow")
            return template.query(
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
        }

        val numeric1List = listOf(
            "bit", "tinyint", "bool", "smallint", "mediumint", "int", "integer", "bigint"
        )
        val numeric2List = listOf(
            "float", "double", "dec"
        )
        val numberSize2Pattern = Regex(".*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\).*")
        val numberSize1Pattern = Regex(".*\\(\\s*(\\d+)\\s*\\).*")

        private fun pattern1Or(type: String, defaultValue: Long): Long {
            val mc = numberSize1Pattern.matchEntire(type) ?: return defaultValue
            return mc.groupValues[1].toLong()
        }
    }

    fun isNumeric1(): Boolean {
        val lowercase = type.lowercase()
        return numeric1List.any {
            lowercase.startsWith(it)
        }
    }

    fun numeric1Level(): Int? {
        val lowercase = type.lowercase()
        return numeric1List.mapIndexed { index, s ->
            if (lowercase.startsWith(s))
                index
            else null
        }.filterNotNull()
            .firstOrNull()
    }

    fun isNumeric2(): Boolean {
        val lowercase = type.lowercase()
        return numeric2List.any {
            lowercase.startsWith(it)
        }
    }

    fun numeric2Level(): Int? {
        val lowercase = type.lowercase()
        return numeric2List.mapIndexed { index, s ->
            if (lowercase.startsWith(s))
                if (index >= 2) {
                    val mc2 = numberSize2Pattern.matchEntire(type)
                    val mc1 = numberSize1Pattern.matchEntire(type)
                    (mc2?.let {
                        it.groupValues[1].toInt() + it.groupValues[2].toInt()
                    } ?: (mc1?.let { it.groupValues[1].toInt() }))?.let { it + 2 }
                        ?: index
                } else
                    index
            else null
        }.filterNotNull()
            .firstOrNull()
    }

    fun toBinLevel(): Long? {
        val lowercase = type.lowercase()
        if (lowercase.startsWith("bin")) {
            return pattern1Or(type, 1)
        }
        if (lowercase.startsWith("varbinary")) {
            return pattern1Or(type, 1)
        }
        if (lowercase.startsWith("tinyblob"))
            return 255
        if (lowercase.startsWith("blob"))
            return 65535
        if (lowercase.startsWith("mediumblob"))
            return 16777215
        if (lowercase.startsWith("longblob"))
            return 4294967295L
        return null
    }

    fun toTextLevel(): Long? {
        val lowercase = type.lowercase()
        if (lowercase.startsWith("char")) {
            return pattern1Or(type, 1)
        }
        if (lowercase.startsWith("varchar")) {
            return pattern1Or(type, 2)
        }
        if (lowercase.startsWith("tinytext"))
            return 255
        if (lowercase.startsWith("text"))
            return 65535
        if (lowercase.startsWith("mediumtext"))
            return 16777215
        if (lowercase.startsWith("longtext"))
            return 4294967295L
        return null
    }


}
