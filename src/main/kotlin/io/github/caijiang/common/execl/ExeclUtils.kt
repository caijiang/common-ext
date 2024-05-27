package io.github.caijiang.common.execl

import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.metadata.data.ReadCellData
import com.alibaba.excel.read.builder.ExcelReaderBuilder
import com.alibaba.excel.read.listener.ReadListener
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.ClientAnchor
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption


/**
 * @author CJ
 */
object ExeclUtils {
    /**
     * 包括
     * 1. 每个 column 的头 title(必然是字符串)
     *
     * @see CopyValue
     */
    data class ExeclResult(
        val columnHeader: Map<Int, String>,
        val data: List<Map<Int, Any?>>
    )

    /**
     * 表示这个值是跟其他字段分享的(从表格中是合并形式的)
     */
    data class CopyValue(
        val value: Any?
    )

    private data class DataLocation(
        val firstRow: Int, val lastRow: Int, val firstColumn: Int, val lastColumn: Int
    ) {
        /**
         * @return 有值的
         */
        fun findData(list: List<Map<Int, Any?>>): Any? {
            return locations().firstNotNullOfOrNull {
                list[it.first][it.second]
            }
        }

        fun locations(): List<Pair<Int, Int>> {
            return firstRow.rangeTo(lastRow)
                .flatMap {
                    firstColumn.rangeTo(lastColumn)
                        .map { c -> it to c }
                }
        }
    }

    data class RowMack(
        val sheetIndex: Int,
        val rowIndex: Int,
        /**
         * 可选的警告
         */
        val rowSummaryWarning: String? = null,
        /**
         * 必须的字段错误，可以为空
         */
        val cellComments: Map<Int, String>
    )

    /**
     * 将一些批注或者警告写入现有 xls
     * @param data 数据
     * @param author 批注作者
     * @param rows 批注警告信息
     */
    @JvmStatic
    fun easyMackXls(data: InputStream, author: String, vararg rows: RowMack): File {
        val book = try {
            XSSFWorkbookFactory.create(data)
        } catch (e: Exception) {
            HSSFWorkbookFactory.create(data)
        }

        book.use { workbook ->
            val factory = workbook.creationHelper
            val sheetRows = rows.groupBy { it.sheetIndex }
            (0).rangeTo(workbook.numberOfSheets).forEach { sheetIndex ->
                sheetRows[sheetIndex]?.let { macks ->
                    val sheet = workbook.getSheetAt(sheetIndex)
                    macks.forEach { mack ->
                        sheet.getRow(mack.rowIndex)?.let { rowData ->
                            mack.cellComments.forEach { (t, u) ->
                                rowData.getCell(t)?.let { cellData ->
                                    val anchor = factory.createClientAnchor()
                                    anchor.anchorType = ClientAnchor.AnchorType.MOVE_AND_RESIZE
                                    anchor.row1 = mack.rowIndex
                                    anchor.row2 = mack.rowIndex + 1
                                    anchor.setCol1(t)
                                    anchor.setCol1(t + 1)

                                    val drawing = sheet.createDrawingPatriarch()
                                    val comment = drawing.createCellComment(anchor)

                                    //set the comment text and author
                                    comment.string = factory.createRichTextString(u)
                                    comment.author = author

                                    cellData.cellComment = comment
                                }

                            }
                            mack.rowSummaryWarning?.let { rowSummaryWarning ->
                                val messageCell = rowData.createCell(rowData.lastCellNum.toInt(), CellType.STRING)
                                val messageStyle = workbook.createCellStyle()
                                messageCell.cellStyle = messageStyle
//                            messageStyle.fillBackgroundColor = IndexedColors.YELLOW.index
                                messageStyle.fillPattern = FillPatternType.FINE_DOTS
                                messageStyle.fillForegroundColor = IndexedColors.YELLOW.index
                                messageStyle.setFont(
                                    workbook.createFont().apply {
                                        color = IndexedColors.RED.index
                                    }
                                )
                                messageCell.setCellValue(rowSummaryWarning)
                            }
                        }
                    }
                }
            }

            val file = File.createTempFile(
                "execl", if (workbook is HSSFWorkbook) {
                    ".xls"
                } else ".xlsx"
            )
            file.deleteOnExit()

            file.outputStream().use {
                workbook.write(it)
                it.flush()
            }
            return file
        }
    }

    /**
     * @param data 数据
     * @param sheetNo sheet
     * @param headRowNumber 默认 1
     * @param block 可选的再处理
     * @return 获取包含行列样式的一切结果 [ExeclResult]
     */
    @JvmStatic
    fun easyReadAll(
        data: InputStream,
        sheetNo: Int = 0,
        headRowNumber: Int = 1,
        block: (ExcelReaderBuilder.() -> Unit)? = null
    ): ExeclResult {
        val temp = File.createTempFile("temp--x-x", "")
        temp.deleteOnExit()
        Files.copy(data, temp.toPath(), StandardCopyOption.REPLACE_EXISTING)

        try {
            val allCellRangeAddress = mutableListOf<CellRangeAddress>()

            try {
                XSSFWorkbookFactory.create(temp).use { book ->
                    allCellRangeAddress.addAll(
                        book.getSheetAt(sheetNo)
                            .mergedRegions
                    )
                }
            } catch (_: Exception) {
            }
            try {
                if (allCellRangeAddress.isEmpty())
                    HSSFWorkbookFactory.create(temp).use { book ->
                        allCellRangeAddress.addAll(
                            book.getSheetAt(sheetNo)
                                .mergedRegions
                        )
                    }
            } catch (_: Exception) {
            }


            val headers = mutableMapOf<Int, String>()

            val list = EasyExcel.read(temp)
                .autoCloseStream(true)
                .useDefaultListener(true)
                .headRowNumber(headRowNumber)
                .apply {
                    block?.invoke(this)
                }
                .registerReadListener(object : ReadListener<MutableMap<Int, Any?>> {
                    override fun invokeHead(headMap: MutableMap<Int, ReadCellData<*>>?, context: AnalysisContext?) {
                        headMap?.forEach { (t, u) ->
                            headers[t] = u.readAsTextOrEmpty()
                        }
                    }

                    override fun invoke(data: MutableMap<Int, Any?>?, context: AnalysisContext?) {
                    }

                    override fun doAfterAllAnalysed(context: AnalysisContext?) {
                    }
                })
                .sheet(sheetNo)
                .doReadSync<MutableMap<Int, Any?>>()

            allCellRangeAddress.forEach {
                // 在 x*y 范围内 找到有值的 然后其他几个都给它
                val location =
                    DataLocation(it.firstRow - headRowNumber, it.lastRow - headRowNumber, it.firstColumn, it.lastColumn)
                location.findData(list)?.let { realValue ->
                    //
                    location.locations().forEach { l ->
                        list[l.first][l.second] = CopyValue(realValue)
                    }
                }
            }

            return ExeclResult(
                headers, list
            )
        } finally {
            temp.delete()
        }


    }

}

private fun <T> ReadCellData<T>.readAsTextOrEmpty(): String {
    return try {
        this.stringValue
    } catch (e: Exception) {
        return this.toString()
    }
}
