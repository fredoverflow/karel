package vm

import parsing.*
import util.IdentityGenerator

class CodeGenerator(private val semantics: KarelSemantics) {

    private val program = vm.instructionBuffer()

    private val pc: Int
        get() = program.size

    private val lastInstruction: Instruction
        get() = program.last()

    private fun removeLastInstruction() {
        program.removeAt(program.lastIndex)
    }

    private fun generateInstruction(bytecode: Int, token: Token) {
        program.add(Instruction(bytecode, token.position))
    }

    private val id = IdentityGenerator()
    private val start = HashMap<Int, Int>()

    private fun translateCalls() {
        program.forEachIndexed { index, instruction ->
            if (instruction.category == CALL) {
                program[index] = instruction.mapTarget { start[it]!! }
            }
        }
    }

    fun generate(): List<Instruction> {
        semantics.reachableCommands.forEach { it.generate() }
        translateCalls()
        return program
    }

    private fun Command.generate() {
        start[id(identifier.lexeme)] = pc
        body.generate()
        generateInstruction(RETURN, body.closingBrace)
    }

    private fun removeNegations(): Int {
        var parity = 0
        while (lastInstruction.bytecode == NOT) {
            removeLastInstruction()
            // If an odd number of NOTs is removed,
            // parity is the bitmask that turns J0MP into J1MP and vice versa.
            parity = parity.xor(J0MP.xor(J1MP))
        }
        return parity
    }

    private fun prepareForwardJump(category: Int, token: Token): Int {
        val parity = removeNegations()
        generateInstruction(category.xor(parity), token)
        return pc - 1
    }

    private fun patchForwardJump(where: Int) {
        program[where] = program[where].withTarget(pc)
    }

    private fun Statement.generate() {
        when (this) {
            is Block -> {
                statements.forEach { it.generate() }
            }
            is IfThen -> {
                condition.generate()
                val over = prepareForwardJump(J0MP, iF)
                th3n.generate()
                patchForwardJump(over)
            }
            is IfThenElse -> {
                condition.generate()
                val overThen = prepareForwardJump(J0MP, iF)
                th3n.generate()
                val overElse = prepareForwardJump(JUMP, th3n.closingBrace)
                patchForwardJump(overThen)
                e1se.generate()
                patchForwardJump(overElse)
            }
            is While -> {
                val back = pc
                condition.generate()
                val over = prepareForwardJump(J0MP, whi1e)
                body.generate()
                generateInstruction(JUMP.or(back), body.closingBrace)
                patchForwardJump(over)
            }
            is Repeat -> {
                generateInstruction(PUSH.or(times), repeat)
                val back = pc
                body.generate()
                generateInstruction(LOOP.or(back), body.closingBrace)
            }
            is Call -> {
                val builtin = builtinCommands[target.lexeme]
                val bytecode = builtin ?: CALL.or(id(target.lexeme))
                generateInstruction(bytecode, target)
            }
        }
    }

    private fun Condition.generate() {
        when (this) {
            is False -> generateInstruction(PUSH.or(0), fa1se)
            is True -> generateInstruction(PUSH.or(1), tru3)

            is OnBeeper -> generateInstruction(ON_BEEPER, onBeeper)
            is BeeperAhead -> generateInstruction(BEEPER_AHEAD, beeperAhead)
            is LeftIsClear -> generateInstruction(LEFT_IS_CLEAR, leftIsClear)
            is FrontIsClear -> generateInstruction(FRONT_IS_CLEAR, frontIsClear)
            is RightIsClear -> generateInstruction(RIGHT_IS_CLEAR, rightIsClear)

            is Not -> {
                p.generate()
                generateInstruction(NOT, not)
            }

            is Conjunction -> {
                p.generate()
                q.generate()
                generateInstruction(AND, and)
            }

            is Disjunction -> {
                p.generate()
                q.generate()
                generateInstruction(OR, or)
            }
        }
    }
}
