package vm

import common.Diagnostic
import logic.World

// If "step over" or "step return" do not finish within 1 second,
// we assume the code contains an infinite loop.
const val TIMEOUT = 1_000_000_000L

// The first instruction starts at address 256.
// This makes it easier to distinguish addresses
// from truth values and loop counters on the stack.
const val ENTRY_POINT = 256

class VirtualMachine(
    private val program: List<Instruction>,
    var world: World,
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

    private fun push(x: Boolean) {
        stack = Stack.Boolean(if (x) 1 else 0, stack)
    }

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
            throw Diagnostic(currentInstruction.position, "infinite loop detected")
        }
    }

    fun executeUserProgram() {
        val start = System.nanoTime()
        while (System.nanoTime() - start < TIMEOUT) {
            repeat(1000) {
                executeOneInstruction()
            }
        }
        throw Diagnostic(currentInstruction.position, "infinite loop detected")
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
                ELSE shr 12 -> pc = if (pop() == 0) target else pc + 1
                THEN shr 12 -> pc = if (pop() != 0) target else pc + 1

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
        pc = pop()
        --callDepth
    }

    private fun executeBasicInstruction(bytecode: Int) {
        when (bytecode) {
            RETURN -> executeReturn()

            MOVE_FORWARD -> world.moveForward().let { world = it; onMove?.invoke(it) }
            TURN_LEFT -> world = world.turnLeft()
            TURN_AROUND -> world = world.turnAround()
            TURN_RIGHT -> world = world.turnRight()
            PICK_BEEPER -> world.pickBeeper().let { world = it; onPickDrop?.invoke(it) }
            DROP_BEEPER -> world.dropBeeper().let { world = it; onPickDrop?.invoke(it) }

            ON_BEEPER -> push(world.onBeeper())
            BEEPER_AHEAD -> push(world.beeperAhead())
            LEFT_IS_CLEAR -> push(world.leftIsClear())
            FRONT_IS_CLEAR -> push(world.frontIsClear())
            RIGHT_IS_CLEAR -> push(world.rightIsClear())

            // INSTRUMENT

            ON_BEEPER_INSTRUMENT -> {
                val status = world.onBeeper()
                push(status)
                currentInstruction.bytecode = if (status) ON_BEEPER_TRUE else ON_BEEPER_FALSE
            }

            BEEPER_AHEAD_INSTRUMENT -> {
                val status = world.beeperAhead()
                push(status)
                currentInstruction.bytecode = if (status) BEEPER_AHEAD_TRUE else BEEPER_AHEAD_FALSE
            }

            LEFT_IS_CLEAR_INSTRUMENT -> {
                val status = world.leftIsClear()
                push(status)
                currentInstruction.bytecode = if (status) LEFT_IS_CLEAR_TRUE else LEFT_IS_CLEAR_FALSE
            }

            FRONT_IS_CLEAR_INSTRUMENT -> {
                val status = world.frontIsClear()
                push(status)
                currentInstruction.bytecode = if (status) FRONT_IS_CLEAR_TRUE else FRONT_IS_CLEAR_FALSE
            }

            RIGHT_IS_CLEAR_INSTRUMENT -> {
                val status = world.rightIsClear()
                push(status)
                currentInstruction.bytecode = if (status) RIGHT_IS_CLEAR_TRUE else RIGHT_IS_CLEAR_FALSE
            }

            // FALSE

            ON_BEEPER_FALSE -> {
                val status = world.onBeeper()
                push(status)
                if (status) currentInstruction.bytecode = ON_BEEPER
            }

            BEEPER_AHEAD_FALSE -> {
                val status = world.beeperAhead()
                push(status)
                if (status) currentInstruction.bytecode = BEEPER_AHEAD
            }

            LEFT_IS_CLEAR_FALSE -> {
                val status = world.leftIsClear()
                push(status)
                if (status) currentInstruction.bytecode = LEFT_IS_CLEAR
            }

            FRONT_IS_CLEAR_FALSE -> {
                val status = world.frontIsClear()
                push(status)
                if (status) currentInstruction.bytecode = FRONT_IS_CLEAR
            }

            RIGHT_IS_CLEAR_FALSE -> {
                val status = world.rightIsClear()
                push(status)
                if (status) currentInstruction.bytecode = RIGHT_IS_CLEAR
            }

            // TRUE

            ON_BEEPER_TRUE -> {
                val status = world.onBeeper()
                push(status)
                if (!status) currentInstruction.bytecode = ON_BEEPER
            }

            BEEPER_AHEAD_TRUE -> {
                val status = world.beeperAhead()
                push(status)
                if (!status) currentInstruction.bytecode = BEEPER_AHEAD
            }

            LEFT_IS_CLEAR_TRUE -> {
                val status = world.leftIsClear()
                push(status)
                if (!status) currentInstruction.bytecode = LEFT_IS_CLEAR
            }

            FRONT_IS_CLEAR_TRUE -> {
                val status = world.frontIsClear()
                push(status)
                if (!status) currentInstruction.bytecode = FRONT_IS_CLEAR
            }

            RIGHT_IS_CLEAR_TRUE -> {
                val status = world.rightIsClear()
                push(status)
                if (!status) currentInstruction.bytecode = RIGHT_IS_CLEAR
            }

            else -> throw IllegalBytecode(bytecode)
        }
        ++pc
    }
}
