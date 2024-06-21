package io.github.caijiang.common.mysql

import com.wix.mysql.distribution.Version
import io.github.caijiang.common.test.solitary.SolitaryHelper
import org.assertj.core.api.Assertions.assertThat
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test


/**
 * @author CJ
 */
class DdlTest {

    @Test
    fun executeScriptResource() {
        val mysql = SolitaryHelper.createMysql(Version.v5_7_latest, null)
        try {
            val template = JdbcTemplate(SolitaryHelper.currentMysqlDatasource())
            Ddl.executeScriptResource(template, ClassPathResource("ddl1.sql"))
            // 这里执行好了 就 建了表
            assertThat(template.queryMysqlTableNames())
                .containsOnly("tb")

            Ddl.executeScriptResource(template, ClassPathResource("ddl2.sql"))
            // 新增了一个字段
            assertThat(template.queryMysqlTableNames())
                .containsOnly("tb", "tbb")
            assertThat(FieldDefine.fromTemplate(template, "tb"))
                .filteredOn {
                    it.field.equals("name", true)
                            && it.type == "varchar(20)"
                            && it.defaultValue == "abc"
                            && it.comment == ""
                            && it.nullable
                }
                .hasSize(1)
            assertThat(FieldDefine.fromTemplate(template, "tb"))
                .filteredOn {
                    it.field.equals("id", true)
                            && it.type == "bigint(20)"
                            && it.comment == ""
                            && !it.nullable
                }
                .hasSize(1)
            // 可以修改的包括 备注,默认值
            // 类型 则包括扩容 varchar  , decimal, int,  text  其他类型的变更只给出警告
            Ddl.executeScriptResource(template, ClassPathResource("ddl3.sql"))
            assertThat(FieldDefine.fromTemplate(template, "tb"))
                .filteredOn {
                    it.field.equals("id", true)
                            && it.type == "bigint(20)"
                            && it.comment == "pk"
                            && !it.nullable
                }
                .hasSize(1)

            Ddl.executeScriptResource(template, ClassPathResource("ddl4.sql"))
            // 字符串扩容
            assertThat(FieldDefine.fromTemplate(template, "tb"))
                .filteredOn {
                    it.field.equals("name", true)
                            && it.type == "varchar(30)"
                            && it.defaultValue == "abc"
                            && it.comment == ""
                            && it.nullable
                }
                .hasSize(1)

            // 缩容是不可接受的哦
            Ddl.executeScriptResource(template, ClassPathResource("ddl3.sql"))
            assertThat(FieldDefine.fromTemplate(template, "tb"))
                .filteredOn {
                    it.field.equals("name", true)
                            && it.type == "varchar(30)"
                            && it.defaultValue == "abc"
                            && it.comment == ""
                            && it.nullable
                }
                .hasSize(1)


        } finally {
            mysql.stop()
        }
    }
}