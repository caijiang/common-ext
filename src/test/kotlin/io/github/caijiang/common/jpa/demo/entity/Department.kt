package io.github.caijiang.common.jpa.demo.entity

import io.github.caijiang.common.*
import io.github.caijiang.common.jpa.demo.jpaEntityEffectiveClass
import io.github.caijiang.common.jpa.demo.jpaHashCode

/**
 * @author CJ
 */
@Entity
data class Department(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Column(insertable = false, updatable = false, nullable = false)
    var id: Long? = null,
    @Column(length = 50)
    val name: String,
    val enabled: Boolean = true,
    @ManyToOne
    var owner: People? = null
) {

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass = other.jpaEntityEffectiveClass()
        val thisEffectiveClass = this.jpaEntityEffectiveClass()
        if (thisEffectiveClass != oEffectiveClass) return false
        other as Department

        return id != null && id == other.id
    }

    override fun hashCode(): Int = jpaHashCode()
}
