package gui

import freditor.Freditor
import freditor.FreditorUI
import freditor.JavaIndenter
import freditor.TabbedEditors
import logic.Problem
import logic.WorldRef
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.JFrame
import javax.swing.border.EmptyBorder

abstract class MainDesign(val worldRef: WorldRef) : JFrame() {

    val controlPanel = ControlPanel(Problem.problems)

    val worldPanel = WorldPanel(worldRef)

    val story = FreditorUI(Flexer, JavaIndenter.instance, 33, 5)

    protected abstract fun createEditor(freditor: Freditor): Editor

    val tabbedEditors = TabbedEditors("karel", Flexer, JavaIndenter.instance, ::createEditor)

    val editor
        get() = tabbedEditors.selectedEditor as Editor

    val virtualMachinePanel = VirtualMachinePanel()

    init {
        title = editor.file.parent.toString()
        val left = verticalBoxPanel(
            controlPanel,
            Box.createVerticalStrut(16),
            worldPanel,
            Box.createVerticalStrut(16),
            story,
        )
        left.border = EmptyBorder(16, 16, 16, 16)
        super.add(left, BorderLayout.WEST)
        super.add(tabbedEditors.tabs, BorderLayout.CENTER)
        super.add(virtualMachinePanel, BorderLayout.EAST)
        super.pack()
        isVisible = true
    }
}
