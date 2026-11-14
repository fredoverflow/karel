package vm

import syntax.lexer.Token
import syntax.parser.Sema
import syntax.tree.*

class Emitter(private val sema: Sema, instrument: Boolean) {

    private val instrumentOffset: Int = if (instrument) ON_BEEPER_INSTRUMENT - ON_BEEPER else 0

    private var instructions = ArrayList<Instruction>()

    private val pc: Int
        get() = instructions.size

    private val labels = ArrayList<Label>()

    private fun label() = Label().also(labels::add)

    private fun emitInstruction(operation: Byte, token: Token): Instruction {
        val instruction = Instruction(operation, 0, token.start)
        instructions.add(instruction)
        return instruction
    }

    private fun emitBranch(predicate: Byte, token: Token, branch: Byte, label: Label, control: Token) {
        emitInstruction((predicate + instrumentOffset).toByte(), token)
        emitInstruction(branch, control).label = label
    }

    private fun inlineJumps() {
        for (label in labels) {
            val instruction = instructions[label.address]
            if (instruction.operation == JUMP) {
                label.address = instruction.label!!.address
            }
        }
    }

    private fun resolveLabels() {
        instructions.forEach(Instruction::resolveLabel)
    }

    fun emit(main: Command): Instructions {
        commandLabels[main] = label()
        main.emit()
        while (toEmit.isNotEmpty()) {
            toEmit.removeFirst().emit()
        }
        inlineJumps()
        resolveLabels()
        return instructions.toTypedArray()
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
                val bodyLabel = label()
                emitInstruction(PUSH, repeat).operand = times
                bodyLabel.address = pc
                body.emit()
                emitInstruction(LOOP, body.closingBrace).label = bodyLabel
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

    private fun Condition.emitPositive(thenLabel: Label, elseLabel: Label, branch: Byte, label: Label, control: Token) {
        when (this) {
            is OnBeeper -> emitBranch(ON_BEEPER, onBeeper, branch, label, control)
            is BeeperAhead -> emitBranch(BEEPER_AHEAD, beeperAhead, branch, label, control)
            is LeftIsClear -> emitBranch(LEFT_IS_CLEAR, leftIsClear, branch, label, control)
            is FrontIsClear -> emitBranch(FRONT_IS_CLEAR, frontIsClear, branch, label, control)
            is RightIsClear -> emitBranch(RIGHT_IS_CLEAR, rightIsClear, branch, label, control)

            is Not -> p.emitNegative(thenLabel, elseLabel, (THEN + ELSE - branch).toByte(), label, control)

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

    private fun Condition.emitNegative(thenLabel: Label, elseLabel: Label, branch: Byte, label: Label, control: Token) {
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

            is Not -> p.emitPositive(thenLabel, elseLabel, (THEN + ELSE - branch).toByte(), label, control)

            is OnBeeper -> emitBranch(ON_BEEPER, onBeeper, branch, label, control)
            is BeeperAhead -> emitBranch(BEEPER_AHEAD, beeperAhead, branch, label, control)
            is LeftIsClear -> emitBranch(LEFT_IS_CLEAR, leftIsClear, branch, label, control)
            is FrontIsClear -> emitBranch(FRONT_IS_CLEAR, frontIsClear, branch, label, control)
            is RightIsClear -> emitBranch(RIGHT_IS_CLEAR, rightIsClear, branch, label, control)
        }
    }
}
