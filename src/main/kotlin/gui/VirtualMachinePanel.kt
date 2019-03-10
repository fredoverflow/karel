package gui

import vm.Instruction

import java.awt.Font

import javax.swing.Box

class VirtualMachinePanel(font: Font) : HorizontalBoxPanel() {

    private val stackTable = StackTable(font)
    private val bytecodeTable = BytecodeTable()

    init {
        add(VerticalBoxPanel(Box.createVerticalGlue(), stackTable))
        add(bytecodeTable)
        isVisible = false
    }

    fun clearStack() {
        stackTable.setStack(Stack.Nil)
    }

    fun setProgram(program: List<Instruction>) {
        bytecodeTable.setProgram(program)
    }

    fun update(pc: Int, stack: Stack<Int>) {
        stackTable.setStack(stack)
        bytecodeTable.highlightLine(pc)
    }
}
