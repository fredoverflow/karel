package gui

import vm.Instruction
import java.awt.Dimension
import java.awt.Font
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableColumn

private val exampleRow = arrayOf("000 ", "8000 ", "JUMP 000 ")

class BytecodeTable(font: Font) : JTable() {
    init {
        this.font = font
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
        model = BytecodeTableModel()

        for (i in 0..2) {
            // see http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableRenderDemoProject/src/components/TableRenderDemo.java
            val column = columnModel.getColumn(i)
            val renderer = getDefaultRenderer(dataModel.getColumnClass(i))
            val component = renderer.getTableCellRendererComponent(this, exampleRow[i], false, false, 0, i)
            column.preferredWidth = component.preferredSize.width
            column.resizable = false
        }

        preferredViewportSize = Dimension(columnModel.columns.asSequence().map(TableColumn::getPreferredWidth).sum(), 1)
        fillsViewportHeight = true
    }

    fun setProgram(program: List<Instruction>) {
        (dataModel as BytecodeTableModel).program = program
    }

    fun highlightLine(line: Int) {
        val row = line - vm.START
        setRowSelectionInterval(row, row)
        scrollToRow(row + 3)
        scrollToRow(row)
    }

    private fun scrollToRow(row: Int) {
        val rectangle = getCellRect(row, 0, true)
        scrollRectToVisible(rectangle)
    }
}

class BytecodeTableModel : AbstractTableModel() {

    var program: List<Instruction> = vm.instructionBuffer()
        set(value) {
            field = value
            fireTableDataChanged()
        }

    override fun getRowCount(): Int = program.size - vm.START

    override fun getColumnCount(): Int = 3

    override fun getColumnName(column: Int): String {
        return when (column) {
            0 -> "@"
            1 -> "code"
            2 -> "mnemonic"

            else -> throw IllegalArgumentException("$column")
        }
    }

    override fun getValueAt(row: Int, column: Int): String {
        val line = row + vm.START
        val instruction = program[line]
        return when (column) {
            0 -> "%3x".format(line)
            1 -> "%4x".format(instruction.bytecode)
            2 -> instruction.mnemonic()

            else -> throw IllegalArgumentException("$column")
        }
    }
}
