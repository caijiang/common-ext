package io.github.caijiang.common.jpa.spring

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * @author CJ
 */
object SpringJpaUtils {

    /**
     * @param pageable   原始分页排序
     * @param direction  方向
     * @param properties 默认排序字段
     * @return 应用了默认排序的 pageable ；如果传入值已明示了排序则不会任何改变
     */
    @JvmStatic
    fun defaultSort(pageable: Pageable, direction: Sort.Direction, vararg properties: String?): Pageable {
        return if (pageable.sort.isUnsorted) {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, direction, *properties)
        } else pageable
    }
}