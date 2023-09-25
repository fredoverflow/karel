package gui

import vm.Instruction
import vm.Stack
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class VirtualMachinePanel : JPanel() {

    private val stackTable = StackTable()
    private val bytecodeTable = BytecodeTable()

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(verticalBoxPanel(Box.createVerticalGlue(), stackTable))
        add(bytecodeTable)
        isVisible = false
    }

    fun setProgram(program: List<Instruction>) {
        bytecodeTable.setProgram(program)
    }

    fun update(stack: Stack?, pc: Int) {
        stackTable.setStack(stack)
        bytecodeTable.highlightLine(pc)
    }
}
