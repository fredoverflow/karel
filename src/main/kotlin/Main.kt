import freditor.SwingConfig
import gui.MainHandler
import javax.swing.SwingUtilities

fun main(args: Array<String>) {
    SwingConfig.nimbusWithDefaultFont(SwingConfig.SANS_SERIF_PLAIN_16)
    SwingUtilities.invokeLater { MainHandler() }
}
