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

    private val labels = ArrayList<Label>()

    private fun label() = Label().also(labels::add)

    private fun emitInstruction(bytecode: Int, token: Token): Instruction {
        val instruction = Instruction(bytecode, token.start)
        program.add(instruction)
        return instruction
    }

    private fun emitBranch(predicate: Int, token: Token, branch: Int, label: Label, control: Token) {
        emitInstruction(predicate + instrumentOffset, token)
        emitInstruction(branch, control).label = label
    }

    private fun inlineJumps() {
        for (label in labels) {
            val instruction = program[label.address]
            if (instruction.category == JUMP) {
                label.address = instruction.label!!.address
            }
        }
    }

    private fun resolveLabels() {
        program.subList(ENTRY_POINT).forEach(Instruction::resolveLabel)
    }

    fun emit(main: Command): List<Instruction> {
        commandLabels[main] = label()
        main.emit()
        while (toEmit.isNotEmpty()) {
            toEmit.removeFirst().emit()
        }
        inlineJumps()
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

    private fun Block.emit() {
        for (statement in statements) {
            statement.emit()
        }
    }

    private fun Statement.emit() {
        when (this) {
            is Call -> {
                val builtin = builtinCommands[target.lexeme]
                if (builtin != null) {
                    emitInstruction(builtin, target)
                } else {
                    val command = sema.command(target.lexeme)!!
                    emitInstruction(CALL, target).label = commandLabels.getOrPut(command) {
                        toEmit.addLast(command)
                        label()
                    }
                }
            }

            is Repeat -> {
                if (between == null) {
                    val bodyLabel = label()
                    emitInstruction(PUSH + times, repeat)
                    bodyLabel.address = pc
                    body.emit()
                    emitInstruction(LOOP, body.closingBrace).label = bodyLabel
                } else {
                    val betweenLabel = label()
                    val bodyLabel = Label()
                    emitInstruction(PUSH + times, repeat)
                    emitInstruction(JUMP, body.openingBrace).label = bodyLabel
                    betweenLabel.address = pc
                    between.emit()
                    bodyLabel.address = pc
                    body.emit()
                    emitInstruction(LOOP, body.closingBrace).label = betweenLabel
                }
            }

            is While -> {
                val bodyLabel = label()
                val checkLabel = label()
                val doneLabel = label()
                emitInstruction(JUMP, whi1e).label = checkLabel
                bodyLabel.address = pc
                body.emit()
                checkLabel.address = pc
                condition.emitPositive(bodyLabel, doneLabel, THEN, bodyLabel, whi1e)
                doneLabel.address = pc
            }

            is IfThenElse -> emit()
        }
    }

    private fun IfThenElse.emit() {
        if (e1se == null) {
            val thenLabel = label()
            val elseLabel = label()
            condition.emitPositive(thenLabel, elseLabel, ELSE, elseLabel, iF)
            thenLabel.address = pc
            th3n.emit()
            elseLabel.address = pc
        } else {
            val thenLabel = label()
            val elseLabel = label()
            val doneLabel = label()
            condition.emitPositive(thenLabel, elseLabel, ELSE, elseLabel, iF)
            thenLabel.address = pc
            th3n.emit()
            emitInstruction(JUMP, th3n.closingBrace).label = doneLabel
            elseLabel.address = pc
            e1se.emit()
            doneLabel.address = pc
        }
    }

    private fun ElseBranch.emit() {
        when (this) {
            is Block -> emit()

            is IfThenElse -> emit()
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
                val right = label()
                p.emitPositive(right, elseLabel, ELSE, elseLabel, and)
                right.address = pc
                q.emitPositive(thenLabel, elseLabel, branch, label, control)
            }

            is Disjunction -> { // left || right
                val right = label()
                p.emitPositive(thenLabel, right, THEN, thenLabel, or)
                right.address = pc
                q.emitPositive(thenLabel, elseLabel, branch, label, control)
            }
        }
    }

    private fun Condition.emitNegative(thenLabel: Label, elseLabel: Label, branch: Int, label: Label, control: Token) {
        when (this) {
            is Disjunction -> { // !(left || right) = !left && !right
                val right = label()
                p.emitNegative(right, elseLabel, THEN, elseLabel, or)
                right.address = pc
                q.emitNegative(thenLabel, elseLabel, branch, label, control)
            }

            is Conjunction -> { // !(left && right) = !left || !right
                val right = label()
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
