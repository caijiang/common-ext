package io.github.caijiang.common.jpa.demo.entity.support

import io.github.caijiang.common.Column
import io.github.caijiang.common.Embeddable
import java.io.Serializable
import java.time.YearMonth


@Embeddable
data class DepartmentMonthPK(
    @Column(name = "m")
    var month: YearMonth,
    var departmentId: Long? = null
) : Serializable
