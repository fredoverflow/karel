package gui

import freditor.*
import logic.Problem
import logic.World
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

abstract class MainDesign(world: World) : JFrame() {

    val controlPanel = ControlPanel(Problem.problems)

    val worldPanel = WorldPanel(world)

    val story = FreditorUI(Flexer, JavaIndenter.instance, 33, 5)

    protected abstract fun createEditor(freditor: Freditor): Editor

    val snippetPanel = SnippetPanel()

    val tabbedEditors = TabbedEditors("karel", Flexer, JavaIndenter.instance, ::createEditor)

    val editor
        get() = tabbedEditors.selectedEditor as Editor

    val virtualMachinePanel = VirtualMachinePanel()

    init {
        title = "karel version ${Release.compilationDate(MainDesign::class.java)} @ ${editor.file.parent}"
        val left = verticalBoxPanel(
            controlPanel,
            Box.createVerticalStrut(16),
            worldPanel,
            Box.createVerticalStrut(16),
            story,
        )
        left.border = EmptyBorder(16, 16, 16, 16)
        super.add(left, BorderLayout.WEST)
        val center = verticalBoxPanel(
            Box.createVerticalStrut(4),
            snippetPanel,
            tabbedEditors.tabs,
        )
        super.add(center, BorderLayout.CENTER)
        super.add(virtualMachinePanel, BorderLayout.EAST)
        super.pack()
        isVisible = true
    }
}
