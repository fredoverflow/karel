package vm

import logic.InfiniteLoop
import logic.StackOverflow
import logic.World

class VirtualMachine(
    private val instructions: Instructions,
    val world: World,
    private val ignoreMove: Boolean = false,
    // callbacks
    private val onCall: ((Instruction, Instruction) -> Unit)? = null,
    private val onReturn: (() -> Unit)? = null,
) {

    var pc: Int = with(instructions[0]) { if (operation == JUMP) operand else 0 }
        private set

    val currentInstruction: Instruction
        get() = instructions[pc]

    val previousInstruction: Instruction
        get() = instructions[pc - 1]

    private val stack = Stack(STACK_SIZE)

    private var stackPointer = STACK_SIZE

    private var callDepth: Int = 0

    fun copyUsedStack(): Stack {
        val sp = stackPointer
        return if (sp < STACK_SIZE) stack.copyOfRange(sp, STACK_SIZE) else EMPTY_STACK
    }

    fun stepInto(virtualMachineVisible: Boolean) {
        executeUnpausedInstructions(virtualMachineVisible)
        executeOneInstruction()
        executeUnpausedInstructions(virtualMachineVisible)
    }

    private fun executeUnpausedInstructions(virtualMachineVisible: Boolean) {
        if (!virtualMachineVisible) {
            while (!currentInstruction.shouldPause()) {
                executeOneInstruction()
            }
        }
    }

    fun stepOver() {
        stepUntil(callDepth)
    }

    fun stepReturn() {
        stepUntil(callDepth - 1)
    }

    private fun stepUntil(targetDepth: Int) {
        stepInto(false)
        var budget = 1_000_000
        while (callDepth > targetDepth) {
            executeOneInstruction()
            if (--budget == 0) throw InfiniteLoop
        }
    }

    fun executeGoal() {
        while (true) {
            executeOneInstruction()
        }
    }

    fun executeGoalPickDropMove() {
        while (executeOneInstruction()) {
            // intentionally empty
        }
    }

    fun executeUserPickDropMove() {
        var budget = 1000
        while (executeOneInstruction()) {
            if (--budget == 0) {
                throw InfiniteLoop
            }
        }
    }

    private fun executeOneInstruction(): Boolean {
        with(currentInstruction) {
            when (+operation) {
                +PUSH -> {
                    val sp = stackPointer - 1
                    if (sp < 0) throw StackOverflow
                    stack[sp] = -operand
                    stackPointer = sp
                    ++pc
                }

                +LOOP -> {
                    val sp = stackPointer
                    if (++stack[sp] < 0) {
                        pc = operand
                    } else {
                        stackPointer = sp + 1
                        ++pc
                    }
                }

                +CALL -> {
                    val sp = stackPointer - 1
                    if (sp < 0) throw StackOverflow
                    stack[sp] = pc
                    stackPointer = sp
                    pc = operand
                    ++callDepth
                    onCall?.invoke(this, findReturnInstructionAfter(operand))
                }

                +JUMP -> {
                    pc = operand
                }

                +RETURN -> {
                    val sp = stackPointer
                    if (sp >= STACK_SIZE) throw Finished
                    pc = stack[sp] + 1
                    stackPointer = sp + 1
                    --callDepth
                    onReturn?.invoke()
                }

                +MOVE_FORWARD -> {
                    world.moveForward()
                    ++pc
                    return ignoreMove
                }

                +TURN_LEFT -> {
                    world.turnLeft()
                    ++pc
                }

                +TURN_AROUND -> {
                    world.turnAround()
                    ++pc
                }

                +TURN_RIGHT -> {
                    world.turnRight()
                    ++pc
                }

                +PICK_BEEPER -> {
                    world.pickBeeper()
                    ++pc
                    return false
                }

                +DROP_BEEPER -> {
                    world.dropBeeper()
                    ++pc
                    return false
                }

                +ON_BEEPER -> {
                    val status = world.onBeeper()
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +BEEPER_AHEAD -> {
                    val status = world.beeperAhead()
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +LEFT_IS_CLEAR -> {
                    val status = world.leftIsClear()
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +FRONT_IS_CLEAR -> {
                    val status = world.frontIsClear()
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +RIGHT_IS_CLEAR -> {
                    val status = world.rightIsClear()
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                // INSTRUMENT

                +ON_BEEPER_INSTRUMENT -> {
                    val status = world.onBeeper()
                    currentInstruction.operation = if (status) ON_BEEPER_TRUE else ON_BEEPER_FALSE
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +BEEPER_AHEAD_INSTRUMENT -> {
                    val status = world.beeperAhead()
                    currentInstruction.operation = if (status) BEEPER_AHEAD_TRUE else BEEPER_AHEAD_FALSE
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +LEFT_IS_CLEAR_INSTRUMENT -> {
                    val status = world.leftIsClear()
                    currentInstruction.operation = if (status) LEFT_IS_CLEAR_TRUE else LEFT_IS_CLEAR_FALSE
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +FRONT_IS_CLEAR_INSTRUMENT -> {
                    val status = world.frontIsClear()
                    currentInstruction.operation = if (status) FRONT_IS_CLEAR_TRUE else FRONT_IS_CLEAR_FALSE
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +RIGHT_IS_CLEAR_INSTRUMENT -> {
                    val status = world.rightIsClear()
                    currentInstruction.operation = if (status) RIGHT_IS_CLEAR_TRUE else RIGHT_IS_CLEAR_FALSE
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                // FALSE

                +ON_BEEPER_FALSE -> {
                    val status = world.onBeeper()
                    if (status) currentInstruction.operation = ON_BEEPER
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +BEEPER_AHEAD_FALSE -> {
                    val status = world.beeperAhead()
                    if (status) currentInstruction.operation = BEEPER_AHEAD
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +LEFT_IS_CLEAR_FALSE -> {
                    val status = world.leftIsClear()
                    if (status) currentInstruction.operation = LEFT_IS_CLEAR
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +FRONT_IS_CLEAR_FALSE -> {
                    val status = world.frontIsClear()
                    if (status) currentInstruction.operation = FRONT_IS_CLEAR
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +RIGHT_IS_CLEAR_FALSE -> {
                    val status = world.rightIsClear()
                    if (status) currentInstruction.operation = RIGHT_IS_CLEAR
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                // TRUE

                +ON_BEEPER_TRUE -> {
                    val status = world.onBeeper()
                    if (!status) currentInstruction.operation = ON_BEEPER
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +BEEPER_AHEAD_TRUE -> {
                    val status = world.beeperAhead()
                    if (!status) currentInstruction.operation = BEEPER_AHEAD
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +LEFT_IS_CLEAR_TRUE -> {
                    val status = world.leftIsClear()
                    if (!status) currentInstruction.operation = LEFT_IS_CLEAR
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +FRONT_IS_CLEAR_TRUE -> {
                    val status = world.frontIsClear()
                    if (!status) currentInstruction.operation = FRONT_IS_CLEAR
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                +RIGHT_IS_CLEAR_TRUE -> {
                    val status = world.rightIsClear()
                    if (!status) currentInstruction.operation = RIGHT_IS_CLEAR
                    with(instructions[pc + 1]) {
                        pc = if (status == (operation == THEN)) operand else pc + 2
                    }
                }

                else -> throw IllegalOperation(operation)
            }
            return true
        }
    }

    private fun findReturnInstructionAfter(start: Int): Instruction {
        var index = start
        while (instructions[index].operation != RETURN) ++index
        return instructions[index]
    }

    @Suppress("ObjectInheritsException")
    object Finished : Exception() {
        private fun readResolve(): Any = Finished
    }
}

fun VirtualMachine.errorCurrent(message: String) {
    currentInstruction.error(message)
}

fun VirtualMachine.errorPrevious(message: String) {
    previousInstruction.error(message)
}
