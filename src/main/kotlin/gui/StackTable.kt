package gui

import freditor.Fronts
import vm.EMPTY_STACK
import vm.ENTRY_POINT
import vm.Stack
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

class StackTable : JComponent() {
    private var stack = EMPTY_STACK

    init {
        resize(0)
    }

    private fun resize(rows: Int) {
        val dimension = Dimension(Fronts.front.width * 5, Fronts.front.height * rows)
        minimumSize = dimension
        maximumSize = dimension
        preferredSize = dimension
        revalidate()
    }

    fun setStack(newStack: Stack) {
        if (newStack.size != stack.size) {
            resize(newStack.size)
        }
        stack = newStack
        repaint()
    }

    override fun paint(graphics: Graphics) {
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)

        var y = 0
        val frontHeight = Fronts.front.height
        val frontRight = 4 * Fronts.front.width

        for (number in stack) {
            if (number >= ENTRY_POINT) {
                Fronts.front.drawHexRight(graphics, frontRight, y, number, 0x808080)
            } else {
                Fronts.front.drawIntRight(graphics, frontRight, y, number, 0x6400c8)
            }
            y += frontHeight
            if (y >= height) break
        }
    }
}
