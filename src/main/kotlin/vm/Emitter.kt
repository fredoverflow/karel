package vm

import common.subList
import syntax.lexer.Token
import syntax.parser.Sema
import syntax.tree.*

class Emitter(private val sema: Sema, instrument: Boolean) {

    private val instrumentOffset: Int = if (instrument) ON_BEEPER_INSTRUMENT - ON_BEEPER else 0

    private val program: MutableList<Instruction> = createInstructionBuffer()

    private val pc: Int
        get() = program.size

    private fun emitInstruction(bytecode: Int, token: Token): Instruction {
        val instruction = Instruction(bytecode, token.start)
        program.add(instruction)
        return instruction
    }

    private fun emitBranch(predicate: Int, token: Token, branch: Int, label: Label, control: Token) {
        emitInstruction(predicate + instrumentOffset, token)
        emitInstruction(branch, control).label = label
    }

    private fun resolveLabels() {
        program.subList(ENTRY_POINT).forEach(Instruction::resolveLabel)
    }

    fun emit(main: Command): List<Instruction> {
        commandLabels[main] = Label()
        main.emit()
        while (toEmit.isNotEmpty()) {
            toEmit.removeFirst().emit()
        }
        resolveLabels()
        return program
    }

    private val commandLabels = HashMap<Command, Label>()

    private val toEmit = ArrayDeque<Command>()

    private fun Command.emit() {
        commandLabels[this]!!.address = pc
        body.emit()
        emitInstruction(RETURN, body.closingBrace)
    }

    private fun Statement.emit() {
        when (this) {
            is Block -> {
                for (statement in statements) {
                    statement.emit()
                }
            }

            is Call -> {
                val builtin = builtinCommands[target.lexeme]
                if (builtin != null) {
                    emitInstruction(builtin, target)
                } else {
                    val command = sema.command(target.lexeme)!!
                    emitInstruction(CALL, target).label = commandLabels.getOrPut(command) {
                        toEmit.addLast(command)
                        Label()
                    }
                }
            }

            is Repeat -> {
                emitInstruction(PUSH + times, repeat)
                val back = pc
                body.emit()
                emitInstruction(LOOP + back, body.closingBrace)
            }

            is While -> {
                val thenLabel = Label()
                val elseLabel = Label()
                val back = pc
                condition.emitPositive(thenLabel, elseLabel, ELSE, elseLabel, whi1e)
                thenLabel.address = pc
                body.emit()
                emitInstruction(JUMP + back, body.closingBrace)
                elseLabel.address = pc
            }

            is IfThenElse -> {
                if (e1se == null) {
                    val thenLabel = Label()
                    val elseLabel = Label()
                    condition.emitPositive(thenLabel, elseLabel, ELSE, elseLabel, iF)
                    thenLabel.address = pc
                    th3n.emit()
                    elseLabel.address = pc
                } else {
                    val thenLabel = Label()
                    val elseLabel = Label()
                    val doneLabel = Label()
                    condition.emitPositive(thenLabel, elseLabel, ELSE, elseLabel, iF)
                    thenLabel.address = pc
                    th3n.emit()
                    emitInstruction(JUMP, th3n.closingBrace).label = doneLabel
                    elseLabel.address = pc
                    e1se.emit()
                    doneLabel.address = pc
                }
            }
        }
    }

    private fun Condition.emitPositive(thenLabel: Label, elseLabel: Label, branch: Int, label: Label, control: Token) {
        when (this) {
            is OnBeeper -> emitBranch(ON_BEEPER, onBeeper, branch, label, control)
            is BeeperAhead -> emitBranch(BEEPER_AHEAD, beeperAhead, branch, label, control)
            is LeftIsClear -> emitBranch(LEFT_IS_CLEAR, leftIsClear, branch, label, control)
            is FrontIsClear -> emitBranch(FRONT_IS_CLEAR, frontIsClear, branch, label, control)
            is RightIsClear -> emitBranch(RIGHT_IS_CLEAR, rightIsClear, branch, label, control)

            is Not -> p.emitNegative(thenLabel, elseLabel, branch xor (ELSE xor THEN), label, control)

            is Conjunction -> { // left && right
                val right = Label()
                p.emitPositive(right, elseLabel, ELSE, elseLabel, and)
                right.address = pc
                q.emitPositive(thenLabel, elseLabel, branch, label, control)
            }

            is Disjunction -> { // left || right
                val right = Label()
                p.emitPositive(thenLabel, right, THEN, thenLabel, or)
                right.address = pc
                q.emitPositive(thenLabel, elseLabel, branch, label, control)
            }
        }
    }

    private fun Condition.emitNegative(thenLabel: Label, elseLabel: Label, branch: Int, label: Label, control: Token) {
        when (this) {
            is Disjunction -> { // !(left || right) = !left && !right
                val right = Label()
                p.emitNegative(right, elseLabel, THEN, elseLabel, or)
                right.address = pc
                q.emitNegative(thenLabel, elseLabel, branch, label, control)
            }

            is Conjunction -> { // !(left && right) = !left || !right
                val right = Label()
                p.emitNegative(thenLabel, right, ELSE, thenLabel, and)
                right.address = pc
                q.emitNegative(thenLabel, elseLabel, branch, label, control)
            }

            is Not -> p.emitPositive(thenLabel, elseLabel, branch xor (ELSE xor THEN), label, control)

            is OnBeeper -> emitBranch(ON_BEEPER, onBeeper, branch, label, control)
            is BeeperAhead -> emitBranch(BEEPER_AHEAD, beeperAhead, branch, label, control)
            is LeftIsClear -> emitBranch(LEFT_IS_CLEAR, leftIsClear, branch, label, control)
            is FrontIsClear -> emitBranch(FRONT_IS_CLEAR, frontIsClear, branch, label, control)
            is RightIsClear -> emitBranch(RIGHT_IS_CLEAR, rightIsClear, branch, label, control)
        }
    }
}
