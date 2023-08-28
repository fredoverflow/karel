package gui

import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

fun horizontalBoxPanel(vararg components: Component) = JPanel().apply {
    layout = BoxLayout(this, BoxLayout.X_AXIS)
    components.forEach(::add)
}

fun verticalBoxPanel(vararg components: Component) = JPanel().apply {
    layout = BoxLayout(this, BoxLayout.Y_AXIS)
    components.forEach(::add)
}

fun JComponent.onKeyPressed(handler: (KeyEvent) -> Unit) {
    addKeyListener(object : KeyAdapter() {
        override fun keyPressed(event: KeyEvent) {
            handler(event)
        }
    })
}
