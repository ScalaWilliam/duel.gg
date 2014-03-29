package us.woop.pinger.testutil

object Tabulator {

  def format(rows: List[List[String]]) = {

    val columnCount =
      rows.maxBy(_.length).length

    val extendedRows =
      for {
        row <- rows
        rowStream = row.toStream #::: Stream.continually("")
        fullRow = rowStream.take(columnCount).toList
      } yield fullRow

    val columnWidths =
      for {
        column <- extendedRows.transpose
        width = column.maxBy(_.length).length
      } yield width

    val newRows =
      for {
        row <- extendedRows
      } yield for {
        (colVal, colNo) <- row.toList.zipWithIndex
        newCol = colVal.toStream #::: Stream.continually(' ')
        colWidth = columnWidths(colNo)
        updatedCell = newCol.take(colWidth).mkString
      } yield updatedCell

    val ret =
      for {
        row <- newRows
        rowStr = s"""| ${row.mkString(" | ")} |"""
      } yield rowStr

    ret.mkString("\n")

  }

}