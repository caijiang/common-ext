package io.github.caijiang.common.jpa.demo.repository

import io.github.caijiang.common.jpa.demo.entity.DepartmentMonthScore
import io.github.caijiang.common.jpa.demo.entity.support.DepartmentMonthPK
import org.springframework.data.jpa.repository.JpaRepository

interface DepartmentMonthScoreRepository : JpaRepository<DepartmentMonthScore, DepartmentMonthPK>