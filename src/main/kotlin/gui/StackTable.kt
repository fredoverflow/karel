package gui

import java.awt.Dimension
import java.awt.Font

import javax.swing.JTable
import javax.swing.table.AbstractTableModel

class StackTable(font: Font) : JTable() {
    init {
        this.font = font
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
        model = StackTableModel()

        val column = columnModel.getColumn(0)
        val renderer = getDefaultRenderer(dataModel.getColumnClass(0))
        val component = renderer.getTableCellRendererComponent(this, "stack", false, false, 0, 0)
        column.preferredWidth = component.preferredSize.width
        column.resizable = false

        preferredViewportSize = Dimension(columnModel.getColumn(0).preferredWidth, 1)
        fillsViewportHeight = true
    }

    fun setStack(stack: Stack<Int>) {
        (dataModel as StackTableModel).stack = stack
    }
}

class StackTableModel : AbstractTableModel() {

    var stack: Stack<Int> = Stack.Nil
        set(value) {
            if (field !== value) {
                field = value
                fireTableDataChanged()
            }
        }

    override fun getRowCount(): Int = stack.size()

    override fun getColumnCount(): Int = 1

    override fun getValueAt(row: Int, column: Int): String = " %3x".format(stack[row])
}
