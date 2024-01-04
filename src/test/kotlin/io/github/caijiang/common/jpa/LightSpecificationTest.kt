package io.github.caijiang.common.jpa

import io.github.caijiang.common.jpa.demo.JpaConfig
import io.github.caijiang.common.jpa.demo.entity.Department
import io.github.caijiang.common.jpa.demo.entity.People
import io.github.caijiang.common.jpa.demo.repository.DepartmentRepository
import io.github.caijiang.common.jpa.demo.repository.PeopleRepository
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import javax.persistence.criteria.JoinType
import kotlin.math.max
import kotlin.test.Test


/**
 * @author CJ
 */
@ContextConfiguration(classes = [JpaConfig::class])
@SpringJUnitConfig
class LightSpecificationTest {

    @Autowired
    private lateinit var peopleRepository: PeopleRepository

    @Autowired
    private lateinit var departmentRepository: DepartmentRepository

    @Test
    fun well() {
        val name = RandomStringUtils.randomAlphabetic(20)
        val nameMatchSpec = LightSpecification<People> { root, _, cb ->
            cb.equal(root.get<String>("name"), name)
        }

        val enabledSpec = LightSpecification<People> { root, _, cb ->
            cb.isTrue(root.get("enabled"))
        }

        assertThat(
            peopleRepository.count(
                nameMatchSpec.or(enabledSpec).toSpring()
            )
        ).isEqualTo(
            max(
                peopleRepository.count(
                    nameMatchSpec.toSpring()
                ), peopleRepository.count(
                    enabledSpec.toSpring()
                )
            )
        )

        val id = peopleRepository.save(People(null, name)).id!!

        assertThat(
            peopleRepository.findOne(
                nameMatchSpec
                    .and(enabledSpec)
                    .toSpring()
            )
        ).isPresent
            .get()
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", id)

        assertThat(
            peopleRepository.count(
                nameMatchSpec.or(enabledSpec).toSpring()
            )
        ).isGreaterThan(0L)

        val nameMatchedCaseFromDepartment = nameMatchSpec.cast<Department> {
            it.join<Department, People>("owner", JoinType.LEFT)
        }
        assertThat(
            departmentRepository.count(
                nameMatchedCaseFromDepartment.toSpring()
            )
        ).isEqualTo(0L)

        departmentRepository.save(Department(name = RandomStringUtils.randomAlphabetic(20)))
        val d1 = departmentRepository.save(Department(name = RandomStringUtils.randomAlphabetic(20)))
        assertThat(
            departmentRepository.count(
                nameMatchedCaseFromDepartment.toSpring()
            )
        ).isEqualTo(0L)
        d1.owner = peopleRepository.getById(id)
        departmentRepository.save(d1)
        assertThat(
            departmentRepository.count(
                nameMatchedCaseFromDepartment.toSpring()
            )
        ).isEqualTo(1L)


    }


}