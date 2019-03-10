package vm

import syntax.lexer.Token
import syntax.tree.*

typealias CommandNameId = Int
typealias Address = Int

class CodeGenerator(private val semantics: KarelSemantics) {

    private val program: MutableList<Instruction> = vm.createInstructionBuffer()

    private val pc: Int
        get() = program.size

    private val lastInstruction: Instruction
        get() = program.last()

    private fun removeLastInstruction() {
        program.removeAt(program.lastIndex)
    }

    private fun generateInstruction(bytecode: Int, token: Token) {
        program.add(Instruction(bytecode, token.start))
    }

    private val id = IdentityGenerator()
    // Forward calls cannot know their target address during code generation.
    // For simplicity, ALL call targets are therefore initially encoded as command name ids.
    // In a subsequent phase, the command name ids are then translated into addresses.
    private val addressOfCommandNameId = HashMap<CommandNameId, Address>()

    private fun translateCallTargets() {
        program.forEachIndexed { index, instruction ->
            if (instruction.category == CALL) {
                program[index] = instruction.mapTarget { addressOfCommandNameId[it]!! }
            }
        }
    }

    fun generate(): List<Instruction> {
        semantics.reachableCommands.forEach { it.generate() }
        translateCallTargets()
        return program
    }

    private fun Command.generate() {
        addressOfCommandNameId[id(identifier.lexeme)] = pc
        body.generate()
        generateInstruction(RETURN, body.closingBrace)
    }

    private fun removeNegations(): Int {
        var parity = 0
        while (lastInstruction.bytecode == NOT) {
            removeLastInstruction()
            // If an odd number of NOTs is removed,
            // parity is the bitmask that turns J0MP into J1MP and vice versa.
            parity = parity xor (J0MP xor J1MP)
        }
        return parity
    }

    private fun prepareForwardJump(category: Int, token: Token): Int {
        val parity = removeNegations()
        generateInstruction(category xor parity, token)
        return pc - 1
    }

    private fun patchForwardJump(from: Int) {
        program[from] = program[from].withTarget(pc)
    }

    private fun Statement.generate() {
        when (this) {
            is Block -> {
                statements.forEach { it.generate() }
            }
            is IfThenElse -> {
                if (e1se == null) {
                    condition.generate()
                    val over = prepareForwardJump(J0MP, iF)
                    th3n.generate()
                    patchForwardJump(over)
                } else {
                    condition.generate()
                    val overThen = prepareForwardJump(J0MP, iF)
                    th3n.generate()
                    val overElse = prepareForwardJump(JUMP, th3n.closingBrace)
                    patchForwardJump(overThen)
                    e1se.generate()
                    patchForwardJump(overElse)
                }
            }
            is While -> {
                val back = pc
                condition.generate()
                val over = prepareForwardJump(J0MP, whi1e)
                body.generate()
                generateInstruction(JUMP or back, body.closingBrace)
                patchForwardJump(over)
            }
            is Repeat -> {
                generateInstruction(PUSH or times, repeat)
                val back = pc
                body.generate()
                generateInstruction(LOOP or back, body.closingBrace)
            }
            is Call -> {
                val builtin = builtinCommands[target.lexeme]
                val bytecode = builtin ?: CALL or id(target.lexeme)
                generateInstruction(bytecode, target)
            }
        }
    }

    private fun Condition.generate() {
        when (this) {
            is False -> generateInstruction(PUSH or 0, fa1se)
            is True -> generateInstruction(PUSH or 1, tru3)

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
