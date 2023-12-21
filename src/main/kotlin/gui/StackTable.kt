package gui

import freditor.Fronts
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

val EMPTY_STACK = IntArray(0)

class StackTable : JComponent() {
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

    var stack = EMPTY_STACK
        set(value) {
            val oldSize = field.size
            val newSize = value.size
            field = value
            if (newSize != oldSize) {
                resize(newSize)
            }
            repaint()
        }

    override fun paint(graphics: Graphics) {
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)

        val frontHeight = Fronts.front.height
        val frontRight = 4 * Fronts.front.width

        var y = stack.size * frontHeight

        for (value in stack) {
            y -= frontHeight
            when (value) {
                0 -> Fronts.front.drawString(graphics, 0, y, "false", 0xff0000)
                -1 -> Fronts.front.drawString(graphics, 0, y, "true", 0x008000)

                in 1..255 -> Fronts.front.drawIntRight(graphics, frontRight, y, value, 0x6400c8)

                else -> Fronts.front.drawHexRight(graphics, frontRight, y, value, 0x808080)
            }
        }
    }
}
