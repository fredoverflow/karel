package gui

import common.subList
import freditor.FreditorUI
import freditor.Indenter
import vm.ENTRY_POINT
import vm.Instruction

class BytecodeTable : FreditorUI(BytecodeFlexer, Indenter.instance, 18, 1) {
    fun setProgram(program: List<Instruction>) {
        load(program.subList(ENTRY_POINT).withIndex()
            .joinToString(prefix = " @  CODE MNEMONIC\n", separator = "\n") { (row, instruction) ->
                "%3x %4x %s".format(row + ENTRY_POINT, instruction.bytecode, instruction.mnemonic())
            })
    }

    fun highlightLine(line: Int) {
        val row = line - ENTRY_POINT + 1
        setCursorTo(row, 9)
    }
}
