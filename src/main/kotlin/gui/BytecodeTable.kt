package gui

import freditor.FreditorUI
import freditor.Indenter
import vm.Instruction

class BytecodeTable : FreditorUI(BytecodeFlexer, Indenter.instance, 18, 1) {
    fun setProgram(program: List<Instruction>) {
        val lines = program.asSequence().drop(vm.ENTRY_POINT).withIndex().map { (row, instruction) ->
            "%3x %4x %s".format(row + vm.ENTRY_POINT, instruction.bytecode, instruction.mnemonic())
        }
        load((sequenceOf(" @  CODE MNEMONIC") + lines).joinToString("\n"))
    }

    fun highlightLine(line: Int) {
        val row = line - vm.ENTRY_POINT + 1
        setCursorTo(row, 9)
    }
}
