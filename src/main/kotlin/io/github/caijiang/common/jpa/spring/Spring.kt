package io.github.caijiang.common.jpa.spring

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort


/**
 * @see SpringJpaUtils.defaultSort
 */
fun Pageable.defaultSort(direction: Sort.Direction, vararg properties: String?): Pageable =
    SpringJpaUtils.defaultSort(this, direction, *properties)
