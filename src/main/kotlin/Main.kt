import gui.MainHandler
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            for (info in UIManager.getInstalledLookAndFeels()) {
                if (info.name == "Nimbus") {
                    UIManager.setLookAndFeel(info.className)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        MainHandler().isVisible = true
    }
}
