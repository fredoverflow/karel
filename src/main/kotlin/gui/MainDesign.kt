package gui

import freditor.Freditor
import freditor.FreditorUI
import freditor.JavaIndenter
import freditor.TabbedEditors
import logic.Problem
import logic.World
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Box
import javax.swing.JFrame

abstract class MainDesign(val atomicWorld: AtomicReference<World>) : JFrame() {

    val controlPanel = ControlPanel(Problem.problems)

    val worldPanel = WorldPanel(atomicWorld)

    val story = FreditorUI(Flexer, JavaIndenter.instance, 33, 5)

    val left = VerticalBoxPanel(controlPanel, worldPanel, Box.createRigidArea(Dimension(0, 16)), story).apply {
        setEmptyBorder(16)
    }

    protected abstract fun createEditor(freditor: Freditor): Editor

    val tabbedEditors = TabbedEditors("karel", Flexer, JavaIndenter.instance, ::createEditor)

    val editor
        get() = tabbedEditors.selectedEditor as Editor

    val virtualMachinePanel = VirtualMachinePanel()

    init {
        title = editor.file.parent.toString()
        add(left, BorderLayout.WEST)
        add(tabbedEditors.tabs, BorderLayout.CENTER)
        add(virtualMachinePanel, BorderLayout.EAST)
        pack()
        isVisible = true
    }
}
