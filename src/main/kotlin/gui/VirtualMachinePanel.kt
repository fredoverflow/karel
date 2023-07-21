package gui

import common.Stack
import vm.Instruction
import vm.StackValue
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

    fun clearStack() {
        stackTable.setStack(Stack.Nil)
    }

    fun setProgram(program: List<Instruction>) {
        bytecodeTable.setProgram(program)
    }

    fun update(pc: Int, stack: Stack<StackValue>) {
        stackTable.setStack(stack)
        bytecodeTable.highlightLine(pc)
    }
}
