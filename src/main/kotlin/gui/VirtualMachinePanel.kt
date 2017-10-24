package gui

import util.Stack
import vm.Instruction
import java.awt.Font
import javax.swing.Box
import javax.swing.JScrollPane

class VirtualMachinePanel(font: Font) : HorizontalBoxPanel() {

    private val bytecodeTable = BytecodeTable(font)
    private val stackTable = StackTable(font)

    init {
        add(JScrollPane(bytecodeTable))
        add(VerticalBoxPanel(Box.createVerticalGlue(), stackTable))
        isVisible = false
    }

    fun setProgram(program: List<Instruction>) {
        bytecodeTable.setProgram(program)
    }

    fun clearStack() {
        stackTable.setStack(Stack.Nil)
    }

    fun update(pc: Int, stack: Stack<Int>) {
        bytecodeTable.highlightLine(pc)
        stackTable.setStack(stack)
    }
}
