package vm

import common.Stack
import common.push
import logic.World
import logic.WorldRef

// If "step over" or "step return" do not finish within 1 second,
// we assume the code contains an infinite loop.
const val TIMEOUT = 1_000_000_000L

// The first instruction starts at address 256.
// This makes it easier to distinguish addresses
// from truth values and loop counters on the stack.
const val ENTRY_POINT = 256

class VirtualMachine(
    private val program: List<Instruction>,
    private val worldRef: WorldRef,
    private val callbacks: Callbacks,
    private val onBeeper: (World) -> Unit = {},
    private val onMove: (World) -> Unit = {},
) {

    interface Callbacks {
        fun onCall(callerPosition: Int, calleePosition: Int) {}
        fun onReturn() {}
        fun onInfiniteLoop() {}
    }

    val world: World
        get() = worldRef.world

    var pc: Int = ENTRY_POINT
        private set

    val currentInstruction: Instruction
        get() = program[pc]

    private val returnInstructionPositions = IntArray(program.size).apply {
        for (index in lastIndex downTo ENTRY_POINT) {
            if (program[index].bytecode == RETURN) {
                this[index] = program[index].position
            } else {
                this[index] = this[index + 1]
            }
        }
    }

    var stack: Stack<StackValue> = Stack.Nil
        private set

    private var callDepth: Int = 0

    private fun push(x: StackValue) {
        stack = stack.push(x)
    }

    private fun push(x: Boolean) {
        stack = stack.push(if (x) Bool.TRUE else Bool.FALSE)
    }

    private fun pop(): StackValue {
        val result = stack.top()
        stack = stack.pop()
        return result
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
            callbacks.onInfiniteLoop()
        }
    }

    fun executeUserProgram() {
        val start = System.nanoTime()
        while (System.nanoTime() - start < TIMEOUT) {
            repeat(1000) {
                executeOneInstruction()
            }
        }
        callbacks.onInfiniteLoop()
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
                ELSE shr 12 -> pc = if (pop() === Bool.FALSE) target else pc + 1
                THEN shr 12 -> pc = if (pop() === Bool.TRUE) target else pc + 1

                else -> throw IllegalBytecode(bytecode)
            }
        }
    }

    private fun Instruction.executePush() {
        push(LoopCounter(target))
        ++pc
    }

    private fun Instruction.executeLoop() {
        val remaining = (pop() as LoopCounter).value - 1
        if (remaining > 0) {
            push(LoopCounter(remaining))
            pc = target
        } else {
            ++pc
        }
    }

    private fun Instruction.executeCall() {
        callbacks.onCall(position, returnInstructionPositions[target])
        push(ReturnAddress(pc))
        ++callDepth
        pc = target
    }

    private fun executeReturn() {
        callbacks.onReturn()
        pc = (pop() as ReturnAddress).value
        --callDepth
    }

    private fun executeBasicInstruction(bytecode: Int) {
        when (bytecode) {
            RETURN -> executeReturn()

            MOVE_FORWARD -> onMove(worldRef.updateAndGet(World::moveForward))
            TURN_LEFT -> worldRef.updateAndGet(World::turnLeft)
            TURN_AROUND -> worldRef.updateAndGet(World::turnAround)
            TURN_RIGHT -> worldRef.updateAndGet(World::turnRight)
            PICK_BEEPER -> onBeeper(worldRef.updateAndGet(World::pickBeeper))
            DROP_BEEPER -> onBeeper(worldRef.updateAndGet(World::dropBeeper))

            ON_BEEPER -> push(worldRef.world.onBeeper())
            BEEPER_AHEAD -> push(worldRef.world.beeperAhead())
            LEFT_IS_CLEAR -> push(worldRef.world.leftIsClear())
            FRONT_IS_CLEAR -> push(worldRef.world.frontIsClear())
            RIGHT_IS_CLEAR -> push(worldRef.world.rightIsClear())

            // INSTRUMENT

            ON_BEEPER_INSTRUMENT -> {
                val status = worldRef.world.onBeeper()
                push(status)
                currentInstruction.bytecode = if (status) ON_BEEPER_TRUE else ON_BEEPER_FALSE
            }

            BEEPER_AHEAD_INSTRUMENT -> {
                val status = worldRef.world.beeperAhead()
                push(status)
                currentInstruction.bytecode = if (status) BEEPER_AHEAD_TRUE else BEEPER_AHEAD_FALSE
            }

            LEFT_IS_CLEAR_INSTRUMENT -> {
                val status = worldRef.world.leftIsClear()
                push(status)
                currentInstruction.bytecode = if (status) LEFT_IS_CLEAR_TRUE else LEFT_IS_CLEAR_FALSE
            }

            FRONT_IS_CLEAR_INSTRUMENT -> {
                val status = worldRef.world.frontIsClear()
                push(status)
                currentInstruction.bytecode = if (status) FRONT_IS_CLEAR_TRUE else FRONT_IS_CLEAR_FALSE
            }

            RIGHT_IS_CLEAR_INSTRUMENT -> {
                val status = worldRef.world.rightIsClear()
                push(status)
                currentInstruction.bytecode = if (status) RIGHT_IS_CLEAR_TRUE else RIGHT_IS_CLEAR_FALSE
            }

            // FALSE

            ON_BEEPER_FALSE -> {
                val status = worldRef.world.onBeeper()
                push(status)
                if (status) currentInstruction.bytecode = ON_BEEPER
            }

            BEEPER_AHEAD_FALSE -> {
                val status = worldRef.world.beeperAhead()
                push(status)
                if (status) currentInstruction.bytecode = BEEPER_AHEAD
            }

            LEFT_IS_CLEAR_FALSE -> {
                val status = worldRef.world.leftIsClear()
                push(status)
                if (status) currentInstruction.bytecode = LEFT_IS_CLEAR
            }

            FRONT_IS_CLEAR_FALSE -> {
                val status = worldRef.world.frontIsClear()
                push(status)
                if (status) currentInstruction.bytecode = FRONT_IS_CLEAR
            }

            RIGHT_IS_CLEAR_FALSE -> {
                val status = worldRef.world.rightIsClear()
                push(status)
                if (status) currentInstruction.bytecode = RIGHT_IS_CLEAR
            }

            // TRUE

            ON_BEEPER_TRUE -> {
                val status = worldRef.world.onBeeper()
                push(status)
                if (!status) currentInstruction.bytecode = ON_BEEPER
            }

            BEEPER_AHEAD_TRUE -> {
                val status = worldRef.world.beeperAhead()
                push(status)
                if (!status) currentInstruction.bytecode = BEEPER_AHEAD
            }

            LEFT_IS_CLEAR_TRUE -> {
                val status = worldRef.world.leftIsClear()
                push(status)
                if (!status) currentInstruction.bytecode = LEFT_IS_CLEAR
            }

            FRONT_IS_CLEAR_TRUE -> {
                val status = worldRef.world.frontIsClear()
                push(status)
                if (!status) currentInstruction.bytecode = FRONT_IS_CLEAR
            }

            RIGHT_IS_CLEAR_TRUE -> {
                val status = worldRef.world.rightIsClear()
                push(status)
                if (!status) currentInstruction.bytecode = RIGHT_IS_CLEAR
            }

            else -> throw IllegalBytecode(bytecode)
        }
        ++pc
    }
}
