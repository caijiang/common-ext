package io.github.caijiang.common.jpa.demo.repository

import io.github.caijiang.common.jpa.demo.entity.People
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface PeopleRepository : JpaRepository<People, Long>, JpaSpecificationExecutor<People>