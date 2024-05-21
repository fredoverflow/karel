package gui

import freditor.FreditorUI
import freditor.JavaIndenter
import freditor.LineNumbers
import logic.Problem
import logic.World

import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicReference

import javax.swing.Box
import javax.swing.JFrame

open class MainDesign(val atomicWorld: AtomicReference<World>) : JFrame() {

    val controlPanel = ControlPanel(Problem.problems)

    val worldPanel = WorldPanel(atomicWorld)

    val story = FreditorUI(Flexer, JavaIndenter.instance, 33, 5)

    val left = VerticalBoxPanel(controlPanel, worldPanel, Box.createRigidArea(Dimension(0, 16)), story).apply {
        setEmptyBorder(16)
    }

    val editor = Editor()
    val editorWithLineNumbers = HorizontalBoxPanel(LineNumbers(editor), editor).apply {
        editor.setComponentToRepaint(this)
    }

    init {
        title = editor.autosaver.pathname
        add(left, BorderLayout.WEST)
        add(editorWithLineNumbers, BorderLayout.CENTER)
        pack()
        isVisible = true
    }
}
