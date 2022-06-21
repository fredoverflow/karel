import freditor.SwingConfig
import gui.MainHandler
import java.awt.EventQueue

fun main() {
    SwingConfig.nimbusWithDefaultFont(SwingConfig.SANS_SERIF_PLAIN_16)
    EventQueue.invokeLater(::MainHandler)
}
