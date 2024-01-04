package io.github.caijiang.common.jpa.spring

import io.github.caijiang.common.jpa.LightSpecification
import io.github.caijiang.common.jpa.demo.JpaConfig
import io.github.caijiang.common.jpa.demo.entity.People
import io.github.caijiang.common.jpa.demo.repository.PeopleRepository
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import kotlin.test.Test

/**
 * @author CJ
 */
@ContextConfiguration(classes = [JpaConfig::class])
@SpringJUnitConfig
class SpringTest {
    @Autowired
    private lateinit var peopleRepository: PeopleRepository

    @Test
    fun defaultSort() {
        val name = RandomStringUtils.randomAlphabetic(20)
        val nameMatchSpec = LightSpecification<People> { root, _, cb ->
            cb.equal(root.get<String>("name"), name)
        }

        peopleRepository.save(People(name = name, age = 20))
        peopleRepository.save(People(name = name, age = 21))

        val pageable = PageRequest.of(0, 1)
        assertThat(
            peopleRepository.findAll(
                nameMatchSpec.toSpring(), pageable.defaultSort(Sort.Direction.DESC, "age")
            ).content[0].age
        ).isEqualTo(21)

        assertThat(
            peopleRepository.findAll(
                nameMatchSpec.toSpring(), pageable.defaultSort(Sort.Direction.ASC, "age")
            ).content[0].age
        ).isEqualTo(20)

    }

}