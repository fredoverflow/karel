package gui

import freditor.FreditorUI
import freditor.Indenter
import vm.Instructions

class InstructionTable : FreditorUI(InstructionFlexer, Indenter.instance, 12, 1) {

    fun setInstructions(instructions: Instructions) {
        load(
            instructions
                .withIndex()
                .joinToString(
                    prefix = " @ INSTRUCT\n",
                    transform = { (row, instruction) -> "%02x %s".format(row, instruction) },
                    separator = "\n",
                )
        )
    }

    fun highlightLine(line: Int) {
        val row = line + 1
        setCursorTo(row, 2)
    }
}
