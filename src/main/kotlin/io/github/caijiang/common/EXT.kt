package io.github.caijiang.common

/**
 * @param target 目标字符串
 * @return 将最后一个目标字符串以及在此之前的所有字符移除 并且返回新的字符串；如果没有找到就返回当前字符串
 */
fun String.removePrefixUntilLast(target: String): String {
    require(target.isNotEmpty())
    val last = lastIndexOf(target)
    if (last == -1) {
        return this
    }
    return substring(last + 1)
}