package vm

import logic.World

// If "step over" or "step return" do not finish within 1 second,
// we assume the code contains an infinite loop.
const val TIMEOUT = 1_000_000_000L

// The first instruction starts at address 256.
// This makes it easier to distinguish addresses
// from truth values and loop counters on the stack.
const val ENTRY_POINT = 256

class VirtualMachine(
    private val program: Array<Instruction>,
    val world: World,
    // callbacks
    private val onCall: ((Instruction, Instruction) -> Unit)? = null,
    private val onReturn: (() -> Unit)? = null,
    private val onPickDrop: ((World) -> Unit)? = null,
    private val onMove: ((World) -> Unit)? = null,
) {

    var pc: Int = ENTRY_POINT
        private set

    val currentInstruction: Instruction
        get() = program[pc]

    var stack: Stack? = null
        private set

    private var callDepth: Int = 0

    private fun pop(): Int {
        val stack = this.stack!!
        this.stack = stack.tail
        return stack.head
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
        val start = System.nanoTime()
        stepInto(false)
        while ((callDepth > targetDepth) && (System.nanoTime() - start < TIMEOUT)) {
            executeOneInstruction()
        }
        if (callDepth > targetDepth) {
            error("infinite loop detected")
        }
    }

    fun executeUserProgram() {
        val start = System.nanoTime()
        while (System.nanoTime() - start < TIMEOUT) {
            repeat(1000) {
                executeOneInstruction()
            }
        }
        error("infinite loop detected")
    }

    fun executeGoalProgram() {
        while (true) {
            executeOneInstruction()
        }
    }

    private fun executeOneInstruction() {
        with(currentInstruction) {
            when (category shr 12) {
                NORM shr 12 -> executeBasicInstruction(bytecode)

                PUSH shr 12 -> executePush()
                LOOP shr 12 -> executeLoop()
                CALL shr 12 -> executeCall()
                JUMP shr 12 -> pc = target

                else -> throw IllegalBytecode(bytecode)
            }
        }
    }

    private fun Instruction.executePush() {
        stack = Stack.LoopCounter(target, stack)
        ++pc
    }

    private fun Instruction.executeLoop() {
        val remaining = pop() - 1
        if (remaining > 0) {
            stack = Stack.LoopCounter(remaining, stack)
            pc = target
        } else {
            ++pc
        }
    }

    private fun Instruction.executeCall() {
        onCall?.invoke(this, findReturnInstructionAfter(target))
        stack = Stack.ReturnAddress(pc, stack)
        ++callDepth
        pc = target
    }

    private fun findReturnInstructionAfter(start: Int): Instruction {
        var index = start
        while (program[index].bytecode != RETURN) ++index
        return program[index]
    }

    object Finished : Exception() {
        private fun readResolve(): Any = Finished
    }

    private fun executeReturn() {
        if (stack == null) throw Finished
        onReturn?.invoke()
        pc = pop() + 1
        --callDepth
    }

    private fun executeBasicInstruction(bytecode: Int) {
        when (bytecode) {
            RETURN -> executeReturn()

            MOVE_FORWARD -> {
                world.moveForward()
                onMove?.invoke(world)
                ++pc
            }

            TURN_LEFT -> {
                world.turnLeft()
                ++pc
            }

            TURN_AROUND -> {
                world.turnAround()
                ++pc
            }

            TURN_RIGHT -> {
                world.turnRight()
                ++pc
            }

            PICK_BEEPER -> {
                world.pickBeeper()
                onPickDrop?.invoke(world)
                ++pc
            }

            DROP_BEEPER -> {
                world.dropBeeper()
                onPickDrop?.invoke(world)
                ++pc
            }

            ON_BEEPER -> {
                val status = world.onBeeper()
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            BEEPER_AHEAD -> {
                val status = world.beeperAhead()
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            LEFT_IS_CLEAR -> {
                val status = world.leftIsClear()
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            FRONT_IS_CLEAR -> {
                val status = world.frontIsClear()
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            RIGHT_IS_CLEAR -> {
                val status = world.rightIsClear()
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            // INSTRUMENT

            ON_BEEPER_INSTRUMENT -> {
                val status = world.onBeeper()
                currentInstruction.bytecode = if (status) ON_BEEPER_TRUE else ON_BEEPER_FALSE
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            BEEPER_AHEAD_INSTRUMENT -> {
                val status = world.beeperAhead()
                currentInstruction.bytecode = if (status) BEEPER_AHEAD_TRUE else BEEPER_AHEAD_FALSE
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            LEFT_IS_CLEAR_INSTRUMENT -> {
                val status = world.leftIsClear()
                currentInstruction.bytecode = if (status) LEFT_IS_CLEAR_TRUE else LEFT_IS_CLEAR_FALSE
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            FRONT_IS_CLEAR_INSTRUMENT -> {
                val status = world.frontIsClear()
                currentInstruction.bytecode = if (status) FRONT_IS_CLEAR_TRUE else FRONT_IS_CLEAR_FALSE
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            RIGHT_IS_CLEAR_INSTRUMENT -> {
                val status = world.rightIsClear()
                currentInstruction.bytecode = if (status) RIGHT_IS_CLEAR_TRUE else RIGHT_IS_CLEAR_FALSE
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            // FALSE

            ON_BEEPER_FALSE -> {
                val status = world.onBeeper()
                if (status) currentInstruction.bytecode = ON_BEEPER
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            BEEPER_AHEAD_FALSE -> {
                val status = world.beeperAhead()
                if (status) currentInstruction.bytecode = BEEPER_AHEAD
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            LEFT_IS_CLEAR_FALSE -> {
                val status = world.leftIsClear()
                if (status) currentInstruction.bytecode = LEFT_IS_CLEAR
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            FRONT_IS_CLEAR_FALSE -> {
                val status = world.frontIsClear()
                if (status) currentInstruction.bytecode = FRONT_IS_CLEAR
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            RIGHT_IS_CLEAR_FALSE -> {
                val status = world.rightIsClear()
                if (status) currentInstruction.bytecode = RIGHT_IS_CLEAR
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            // TRUE

            ON_BEEPER_TRUE -> {
                val status = world.onBeeper()
                if (!status) currentInstruction.bytecode = ON_BEEPER
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            BEEPER_AHEAD_TRUE -> {
                val status = world.beeperAhead()
                if (!status) currentInstruction.bytecode = BEEPER_AHEAD
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            LEFT_IS_CLEAR_TRUE -> {
                val status = world.leftIsClear()
                if (!status) currentInstruction.bytecode = LEFT_IS_CLEAR
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            FRONT_IS_CLEAR_TRUE -> {
                val status = world.frontIsClear()
                if (!status) currentInstruction.bytecode = FRONT_IS_CLEAR
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            RIGHT_IS_CLEAR_TRUE -> {
                val status = world.rightIsClear()
                if (!status) currentInstruction.bytecode = RIGHT_IS_CLEAR
                with(program[pc + 1]) {
                    pc = if (status == (category == THEN)) target else pc + 2
                }
            }

            else -> throw IllegalBytecode(bytecode)
        }
    }
}

fun VirtualMachine.error(message: String) {
    currentInstruction.error(message)
}
