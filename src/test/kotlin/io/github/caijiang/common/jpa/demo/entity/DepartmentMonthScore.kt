package io.github.caijiang.common.jpa.demo.entity

import io.github.caijiang.common.EmbeddedId
import io.github.caijiang.common.Entity
import io.github.caijiang.common.ManyToOne
import io.github.caijiang.common.MapsId
import io.github.caijiang.common.jpa.demo.entity.support.DepartmentMonthPK
import io.github.caijiang.common.jpa.jpaEntityEffectiveClass
import io.github.caijiang.common.jpa.jpaEntityHashCode
import java.time.YearMonth

/**
 * 部门业绩，一个一个月的来
 */
@Entity
data class DepartmentMonthScore(
//    @Transient
//    val month: YearMonth,
    @MapsId("departmentId")
    @ManyToOne
    val department: Department,
    @EmbeddedId
    val pk: DepartmentMonthPK = DepartmentMonthPK(YearMonth.now(), department.id!!),
    val score: Int,

    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass = other.jpaEntityEffectiveClass()
        val thisEffectiveClass = jpaEntityEffectiveClass()
        if (thisEffectiveClass != oEffectiveClass) return false
        other as DepartmentMonthScore

        return pk == other.pk
    }

    override fun hashCode(): Int = jpaEntityHashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(pk = $pk )"
    }
}
