package gui

import freditor.Fronts
import vm.Stack
import vm.forEach
import vm.size
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

class StackTable : JComponent() {
    private var stack: Stack? = null

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

    fun setStack(newStack: Stack?) {
        if (newStack !== stack) {
            if (newStack.size != stack.size) {
                resize(newStack.size)
            }
            stack = newStack
            repaint()
        }
    }

    override fun paint(graphics: Graphics) {
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)

        var y = 0
        val frontHeight = Fronts.front.height
        val frontRight = 4 * Fronts.front.width

        stack.forEach { stack ->
            when (stack) {
                is Stack.ReturnAddress -> Fronts.front.drawHexRight(graphics, frontRight, y, stack.head, 0x808080)

                is Stack.LoopCounter -> Fronts.front.drawIntRight(graphics, frontRight, y, stack.head, 0x6400c8)
            }
            y += frontHeight
        }
    }
}
