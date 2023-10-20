package gui

import java.awt.Toolkit.getDefaultToolkit

val screenHeight: Int
    get() = getDefaultToolkit().screenSize.height

fun flushGraphicsBuffers() {
    getDefaultToolkit().sync()
}
