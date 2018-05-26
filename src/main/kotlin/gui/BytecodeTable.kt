package gui

import freditor.FreditorUI
import freditor.Indenter
import vm.Instruction

class BytecodeTable : FreditorUI(BytecodeFlexer.instance, Indenter.instance, 18, 1) {
    fun setProgram(program: List<Instruction>) {
        val lines = program.asSequence().drop(vm.START).withIndex().map { (row, instruction) ->
            "%3x %4x %s".format(row + vm.START, instruction.bytecode, instruction.mnemonic())
        }
        loadFromString((sequenceOf(" @  CODE MNEMONIC") + lines).joinToString("\n"))
    }

    fun highlightLine(line: Int) {
        val row = line - vm.START + 1
        setCursorTo(row, 9)
    }
}
