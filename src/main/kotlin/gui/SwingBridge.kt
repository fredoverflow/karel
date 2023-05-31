package gui

import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

open class HorizontalBoxPanel(vararg components: Component) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        components.forEach { add(it) }
    }
}

open class VerticalBoxPanel(vararg components: Component) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        components.forEach { add(it) }
    }
}

fun JComponent.onMouseClicked(handler: (MouseEvent) -> Unit) {
    addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(event: MouseEvent) {
            handler(event)
        }
    })
}

fun JComponent.onKeyPressed(handler: (KeyEvent) -> Unit) {
    addKeyListener(object : KeyAdapter() {
        override fun keyPressed(event: KeyEvent) {
            handler(event)
        }
    })
}

fun JComponent.setEmptyBorder(size: Int) {
    border = EmptyBorder(size, size, size, size)
}
