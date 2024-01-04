package io.github.caijiang.common.jpa.demo.entity

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.MappedSuperclass

/**
 * @author CJ
 */
@MappedSuperclass
//@Inheritance(strategy = InheritanceType.JOINED)
abstract class Animal
    (
    var gender: Boolean = false,
    var birthTime: LocalDateTime = LocalDateTime.now(),
    var birthDate: LocalDate = LocalDate.now(),
)