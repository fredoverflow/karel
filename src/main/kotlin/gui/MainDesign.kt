package gui

import freditor.FreditorUI
import freditor.JavaIndenter
import freditor.LineNumbers
import logic.KarelWorld
import logic.World
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Box
import javax.swing.JFrame
import javax.swing.JOptionPane

open class MainDesign(val atomicKarel: AtomicReference<KarelWorld>) : JFrame(Editor.filename) {

    val controlPanel = ControlPanel(World.problemDirectory)

    val karelPanel = KarelPanel(atomicKarel)

    val story = FreditorUI(Flexer.instance, JavaIndenter(Flexer.instance), 33, 5)

    val left = VerticalBoxPanel(controlPanel, karelPanel, Box.createRigidArea(Dimension(0, 16)), story).apply {
        setEmptyBorder(16)
    }

    val editor = Editor()
    val editorWithLineNumbers = HorizontalBoxPanel(LineNumbers(editor), editor).apply {
        editor.setComponentToRepaint(this)
    }

    val virtualMachinePanel = VirtualMachinePanel(Font(Font.MONOSPACED, Font.PLAIN, 16))

    init {
        add(left, BorderLayout.WEST)
        add(editorWithLineNumbers, BorderLayout.CENTER)
        add(virtualMachinePanel, BorderLayout.EAST)

        pack()
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    fun showErrorDialog(message: String, title: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
    }
}
