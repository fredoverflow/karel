package gui

import common.Stack
import freditor.Front
import vm.StackValue
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

class StackTable : JComponent() {
    private var stack: Stack<StackValue> = Stack.Nil

    init {
        resize(0)
    }

    private fun resize(rows: Int) {
        val dimension = Dimension(Front.front.width * 5, Front.front.height * rows)
        minimumSize = dimension
        maximumSize = dimension
        preferredSize = dimension
        revalidate()
    }

    fun setStack(stack: Stack<StackValue>) {
        if (stack !== this.stack) {
            val newSize = stack.size()
            val oldSize = this.stack.size()
            this.stack = stack
            if (newSize != oldSize) {
                resize(newSize)
            }
            repaint()
        }
    }

    override fun paint(g: Graphics) {
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)

        var y = 0
        val frontHeight = Front.front.height
        stack.forEach { stackValue ->
            Front.front.drawString(g, 0, y, stackValue.toString(), stackValue.color)
            y += frontHeight
        }
    }
}
