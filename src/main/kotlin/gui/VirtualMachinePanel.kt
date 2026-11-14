package gui

import vm.Instructions
import vm.Stack
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class VirtualMachinePanel : JPanel() {

    private val stackTable = StackTable()
    private val instructionTable = InstructionTable()

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(verticalBoxPanel(Box.createVerticalGlue(), stackTable))
        add(instructionTable)
        isVisible = false
    }

    fun setInstructions(instructions: Instructions) {
        instructionTable.setInstructions(instructions)
    }

    fun update(stack: Stack, pc: Int) {
        stackTable.setStack(stack)
        instructionTable.highlightLine(pc)
    }
}
