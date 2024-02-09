package io.github.caijiang.common.jpa.demo.entity

import io.github.caijiang.common.*
import io.github.caijiang.common.jpa.jpaEntityEffectiveClass
import io.github.caijiang.common.jpa.jpaEntityHashCode
import java.util.*

/**
 * @author CJ
 */
@Entity
data class People(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Column(insertable = false, updatable = false, nullable = false)
    var id: Long? = null,
    @Column(length = 50)
    val name: String,
    val age: Int = 0,
    val enabled: Boolean = true,
    val nullableText: String? = null,
    @ElementCollection
    val favorites: MutableSet<String> = Collections.emptySet(),
    @ElementCollection
    val tags: MutableCollection<String> = Collections.emptySet(),
    @ManyToMany
    val friends: MutableList<People> = Collections.emptyList(),
    @ElementCollection
    val attributes: MutableMap<String, String> = Collections.emptyMap(),
    @ManyToOne
    val belongDepartment: Department? = null,
) : Animal() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass = other.jpaEntityEffectiveClass()
        val thisEffectiveClass = jpaEntityEffectiveClass()
        if (thisEffectiveClass != oEffectiveClass) return false
        other as People

        return id != null && id == other.id
    }

    override fun hashCode(): Int = jpaEntityHashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , name = $name , age = $age )"
    }

}