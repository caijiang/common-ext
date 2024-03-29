package io.github.caijiang.common.jpa

import io.github.caijiang.common.EntityManagerFactory
import io.github.caijiang.common.JoinType
import io.github.caijiang.common.jpa.demo.JpaConfig
import io.github.caijiang.common.jpa.demo.entity.Animal
import io.github.caijiang.common.jpa.demo.entity.Department
import io.github.caijiang.common.jpa.demo.entity.People
import io.github.caijiang.common.jpa.demo.repository.PeopleRepository
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.time.LocalDateTime
import kotlin.test.Test

/**
 * @author CJ
 */
@ContextConfiguration(classes = [JpaConfig::class])
@SpringJUnitConfig
class DSLTest {
    @Autowired
    private lateinit var peopleRepository: PeopleRepository

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Test
    fun cache() {

        val name = RandomStringUtils.randomAlphabetic(20)
        val id = peopleRepository.save(People(name = name, age = 10)).id!!

        peopleRepository.getReferenceById(id)
        val entityManager = entityManagerFactory.createEntityManager()
        entityManager.removeAllCache()
        peopleRepository.getReferenceById(id)

    }

    @Test
    fun join() {
        println(Department::class.isOpen)
        val name = RandomStringUtils.randomAlphabetic(20)
        val entityManager = entityManagerFactory.createEntityManager()
        entityManager.createCriteriaQuery(People::class.java, LightSpecification.where(null))
            .resultList

        entityManager.createCriteriaQueryWithTuple(People::class.java, { root, _ ->
            this.multiselect(root.get(People::name))
        })
            .resultList

        entityManager.createCriteriaQueryWithTuple(People::class.java, { root, _ ->
            val joinSet = root.joinSet(People::favorites)
            val joinCollection = root.joinCollection(People::tags)
            val joinMap = root.joinMap(People::attributes)
            this.multiselect(
                joinSet,
                joinCollection,
                root.joinList(People::friends, JoinType.LEFT),
                joinMap.key(),
                joinMap.value(),
                root.joinSet(People::favorites)
            )
        })
            .resultList

        entityManager.createCriteriaQueryWithTuple(People::class.java, { root, _ ->
            multiselect(
                root.joinSingle(People::belongDepartment).get(Department::name)
            )
        }).resultList
        // 单独属性时 我约定的是它所在的类的规格
        // 或者反过来说 X的规格 也可以作用在X 所在的类
        entityManager.createCriteriaQuery(
            People::class.java, Department::name.equal(name)
                .specificationSingleRelation(People::belongDepartment)
        )



    }

    @Test
    fun `in`() {
        val names = listOf(RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20))

        assertThat(
            peopleRepository.count(
                People::name.`in`(emptySet()).toSpring()
            )
        ).isEqualTo(0L)

        val spec = People::name.`in`(names).toSpring()
        assertThat(peopleRepository.count(spec)).isEqualTo(0L)
        peopleRepository.save(People(name = names.random()))
        assertThat(peopleRepository.count(spec)).isEqualTo(1L)
        peopleRepository.save(People(name = names.random()))
        assertThat(peopleRepository.count(spec)).isEqualTo(2L)
    }

    @Test
    fun comparison() {
        val name = RandomStringUtils.randomAlphabetic(20)
        peopleRepository.save(People(name = name, age = 10))
        peopleRepository.save(People(name = name, age = 11))
        peopleRepository.save(People(name = name, age = 12))
        peopleRepository.save(People(name = name, age = 13))

        val nameSpec = People::name.equal(name)

        assertThat(peopleRepository.count(nameSpec.and(People::age.between(9, 10)).toSpring()))
            .isEqualTo(1)
        assertThat(peopleRepository.count(nameSpec.and(People::age.between(10, 11)).toSpring()))
            .isEqualTo(2)
        assertThat(peopleRepository.count(nameSpec.and(People::age.between(10, 13)).toSpring()))
            .isEqualTo(4)
        assertThat(peopleRepository.count(nameSpec.and(People::age.between(10, 14)).toSpring()))
            .isEqualTo(4)

        assertThat(
            peopleRepository.count(People::age.lt(10).and(nameSpec).toSpring())
        ).isEqualTo(0L)
        assertThat(
            peopleRepository.count(People::age.lessThan(10).and(nameSpec).toSpring())
        ).isEqualTo(0L)
        assertThat(
            peopleRepository.count(People::age.le(10).and(nameSpec).toSpring())
        ).isEqualTo(1L)
        assertThat(
            peopleRepository.count(People::age.lessThanOrEqualTo(10).and(nameSpec).toSpring())
        ).isEqualTo(1L)
        assertThat(
            peopleRepository.count(People::age.lt(11).and(nameSpec).toSpring())
        ).isEqualTo(1L)
        assertThat(
            peopleRepository.count(People::age.lessThan(11).and(nameSpec).toSpring())
        ).isEqualTo(1L)

        assertThat(
            peopleRepository.count(People::age.gt(13).and(nameSpec).toSpring())
        ).isEqualTo(0L)
        assertThat(
            peopleRepository.count(People::age.greaterThan(13).and(nameSpec).toSpring())
        ).isEqualTo(0L)
        assertThat(
            peopleRepository.count(People::age.ge(13).and(nameSpec).toSpring())
        ).isEqualTo(1L)
        assertThat(
            peopleRepository.count(People::age.greaterThanOrEqualTo(13).and(nameSpec).toSpring())
        ).isEqualTo(1L)
        assertThat(
            peopleRepository.count(People::age.gt(12).and(nameSpec).toSpring())
        ).isEqualTo(1L)
        assertThat(
            peopleRepository.count(People::age.greaterThan(12).and(nameSpec).toSpring())
        ).isEqualTo(1L)
    }

    @Test
    fun `string like`() {
        val name = RandomStringUtils.randomAlphabetic(20)
        val nameSpec = People::name.equal(name)
        peopleRepository.save(People(name = name, nullableText = "hello"))
        assertThat(peopleRepository.count(People::nullableText.like("hello").and(nameSpec).toSpring()))
            .isEqualTo(1)
        assertThat(peopleRepository.count(People::nullableText.like("hello", '\\').and(nameSpec).toSpring()))
            .isEqualTo(1)
        assertThat(peopleRepository.count(People::nullableText.notLike("hi").and(nameSpec).toSpring()))
            .isEqualTo(1)
        assertThat(peopleRepository.count(People::nullableText.notLike("hi", '\\').and(nameSpec).toSpring()))
            .isEqualTo(1)
    }

    @Test
    @Disabled(value = "鉴于不同的 jpa 对于集合类关联差别太大，所以不再这里作强制要求。")
    fun `collections for attributes`() {
        val name = RandomStringUtils.randomAlphabetic(20)
        peopleRepository.save(People(name = name, favorites = mutableSetOf("Sport", "Movie")))

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::favorites.isEmpty()
                ).toSpring()
            )
        ).isEqualTo(0L)

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::favorites.isNotEmpty()
                ).toSpring()
            )
        ).isEqualTo(1L)

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::favorites.isMember("Photo")
                ).toSpring()
            )
        ).isEqualTo(0L)

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::favorites.isMember("Sport")
                ).toSpring()
            )
        ).isEqualTo(1L)

        /*
        EL : SELECT COUNT(t0.ID) FROM PEOPLE t0, People_FAVORITES t1 WHERE (((t0.NAME = ?) AND (t1.FAVORITES <> ?)) AND (t1.People_ID = t0.ID))
        Hibernate: select count(people0_.id) as col_0_0_ from People people0_ where people0_.name=? and (? not in  (select favorites1_.favorites from People_favorites favorites1_ where people0_.id=favorites1_.People_id))
        */
        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::favorites.isNotMember("Photo")
                ).toSpring()
            )
        ).isEqualTo(1L)

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::favorites.isNotMember("Sport")
                ).toSpring()
            )
        ).isEqualTo(0L)

    }

    @Test
    fun equality() {
        val name = RandomStringUtils.randomAlphabetic(20)

        peopleRepository.save(People(name = name))

        assertThat(
            peopleRepository.count(People::name.equal(name).toSpring())
        ).isEqualTo(1)

        assertThat(
            peopleRepository.count(People::name.notEqual(name).toSpring())
        ).isEqualTo(
            peopleRepository.count() -
                    peopleRepository.count(People::name.equal(name).toSpring())
        )
    }

    @Test
    fun trueOrFalse() {
        val name = RandomStringUtils.randomAlphabetic(20)
        peopleRepository.save(People(name = name)
            .apply {
                gender = true
            })

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::gender.isTrue()
                ).toSpring()
            )
        ).isEqualTo(1)

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::gender.isFalse()
                ).toSpring()
            )
        ).isEqualTo(0)

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::gender.isTrue().not()
                ).toSpring()
            )
        ).isEqualTo(0)

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::nullableText.isNull()
                ).toSpring()
            )
        ).isEqualTo(1)

        assertThat(
            peopleRepository.count(
                People::name.equal(name).and(
                    People::nullableText.isNotNull()
                ).toSpring()
            )
        ).isEqualTo(0)
    }

    @Test
    fun dateTimeRange() {
        val now = LocalDateTime.now()
        val name = RandomStringUtils.randomAlphabetic(20)
        val nameSpec = People::name.equal(name)
        val birthTimeSpec = Animal::birthTime.between(now.minusYears(1), now.plusYears(1))
        val spec = nameSpec.and(birthTimeSpec.upcast())

        peopleRepository.save(People(name = name).apply {
            birthTime = now.minusYears(2)
            birthDate = now.minusYears(2).toLocalDate()
        })

        assertThat(peopleRepository.count(nameSpec.toSpring()))
            .isEqualTo(1)

        assertThat(peopleRepository.count(spec.toSpring()))
            .isEqualTo(0)

        assertThat(
            peopleRepository.count(
                nameSpec.and(
                    Animal::birthDate.between(
                        now.minusYears(1).toLocalDate(),
                        now.plusYears(1).toLocalDate()
                    )
                        .upcast()
                ).toSpring()
            )
        )
            .isEqualTo(0)

        assertThat(
            peopleRepository.count(
                nameSpec.and(
                    Animal::birthDate.between(
                        now.minusYears(1).toLocalDate(),
                        now.plusYears(1).toLocalDate()
                    )
                        .upcast()
                ).toSpring()
            )
        )
            .isEqualTo(0)

    }

}