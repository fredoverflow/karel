package vm

import common.Stack
import common.push
import logic.World
import java.util.concurrent.atomic.AtomicReference

// If "step over" or "step return" do not finish within 1 second,
// we assume the code contains an infinite loop.
const val TIMEOUT = 1_000_000_000L

// The first instruction starts at address 256.
// This makes it easier to distinguish addresses
// from truth values and loop counters on the stack.
const val ENTRY_POINT = 256

class VirtualMachine(
    private val program: List<Instruction>,
    private val atomicWorld: AtomicReference<World>,
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
        get() = atomicWorld.get()

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
            when (category) {
                NORM -> executeBasicInstruction(bytecode)

                PUSH -> executePush()
                LOOP -> executeLoop()
                CALL -> executeCall()

                JUMP -> pc = target
                ELSE -> pc = if (pop() === Bool.FALSE) target else pc + 1
                THEN -> pc = if (pop() === Bool.TRUE) target else pc + 1

                else -> throw IllegalBytecode(bytecode)
            }
        }
    }

    private fun Instruction.executePush() {
        push(
            when (target) {
                0 -> Bool.FALSE
                1 -> Bool.TRUE
                else -> LoopCounter(target)
            }
        )
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

            MOVE_FORWARD -> onMove(atomicWorld.updateAndGet(World::moveForward))
            TURN_LEFT -> atomicWorld.updateAndGet(World::turnLeft)
            TURN_AROUND -> atomicWorld.updateAndGet(World::turnAround)
            TURN_RIGHT -> atomicWorld.updateAndGet(World::turnRight)
            PICK_BEEPER -> onBeeper(atomicWorld.updateAndGet(World::pickBeeper))
            DROP_BEEPER -> onBeeper(atomicWorld.updateAndGet(World::dropBeeper))

            ON_BEEPER -> push(atomicWorld.get().onBeeper())
            BEEPER_AHEAD -> push(atomicWorld.get().beeperAhead())
            LEFT_IS_CLEAR -> push(atomicWorld.get().leftIsClear())
            FRONT_IS_CLEAR -> push(atomicWorld.get().frontIsClear())
            RIGHT_IS_CLEAR -> push(atomicWorld.get().rightIsClear())

            NOT -> push(pop() === Bool.FALSE)
            AND -> push((pop() === Bool.TRUE) and (pop() === Bool.TRUE))
            OR -> push((pop() === Bool.TRUE) or (pop() === Bool.TRUE))
            XOR -> push((pop() === Bool.TRUE) xor (pop() === Bool.TRUE))

            else -> throw IllegalBytecode(bytecode)
        }
        ++pc
    }
}
