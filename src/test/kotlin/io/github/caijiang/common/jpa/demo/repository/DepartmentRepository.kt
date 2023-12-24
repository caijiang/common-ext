package io.github.caijiang.common.jpa.demo.repository

import io.github.caijiang.common.jpa.demo.entity.Department
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface DepartmentRepository : JpaRepository<Department, Long>, JpaSpecificationExecutor<Department>