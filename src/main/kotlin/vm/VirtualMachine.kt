package vm

import logic.World
import util.Stack
import util.push

import java.util.concurrent.atomic.AtomicReference

// If "step over" or "step return" do not finish within 10 seconds,
// we assume the code contains an infinite loop.
const val TIMEOUT = 10000000000L

// The first instruction starts at address 256.
// This makes it easier to distinguish addresses
// from truth values and loop counters on the stack.
const val START = 256

data class IllegalBytecode(val bytecode: Int) : Exception("%04x".format(bytecode))

class VirtualMachine(val program: List<Instruction>,
                     private val atomicWorld: AtomicReference<World>,
                     private val onCall: (Int, Int) -> Unit,
                     private val onReturn: () -> Unit,
                     private val onInfiniteLoop: () -> Unit) {

    var pc: Int = vm.START
        private set(value) {
            if (field !in vm.START..program.size) throw IllegalArgumentException("$value")
            field = value
        }

    val currentInstruction: Instruction
        get() = program[pc]

    var stack: Stack<Int> = Stack.Nil
        private set

    private var callDepth: Int = 0

    private fun push(x: Int) {
        stack = stack.push(x)
    }

    private fun push(x: Boolean) {
        stack = stack.push(if (x) 1 else 0)
    }

    private fun pop(): Int {
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
        while ((callDepth > targetDepth) && (System.nanoTime() - start < vm.TIMEOUT)) {
            executeOneInstruction()
        }
        if (callDepth > targetDepth) {
            onInfiniteLoop()
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
                J0MP -> pc = if (pop() == 0) target else pc + 1
                J1MP -> pc = if (pop() != 0) target else pc + 1

                else -> throw IllegalBytecode(bytecode)
            }
        }
    }

    private fun Instruction.executePush() {
        push(target)
        ++pc
    }

    private fun Instruction.executeLoop() {
        val remaining = pop() - 1
        if (remaining > 0) {
            push(remaining)
            pc = target
        } else {
            ++pc
        }
    }

    private fun Instruction.executeCall() {
        val returnInstruction = program.asSequence().drop(target).find { it.bytecode == RETURN }
        onCall(position, returnInstruction!!.position)
        push(pc)
        ++callDepth
        pc = target
    }

    private fun executeReturn() {
        onReturn()
        pc = pop()
        --callDepth
    }

    private fun executeBasicInstruction(bytecode: Int) {
        when (bytecode) {
            RETURN -> executeReturn()

            MOVE_FORWARD -> atomicWorld.updateAndGet(World::moveForward)
            TURN_LEFT -> atomicWorld.updateAndGet(World::turnLeft)
            TURN_AROUND -> atomicWorld.updateAndGet(World::turnAround)
            TURN_RIGHT -> atomicWorld.updateAndGet(World::turnRight)
            PICK_BEEPER -> atomicWorld.updateAndGet(World::pickBeeper)
            DROP_BEEPER -> atomicWorld.updateAndGet(World::dropBeeper)

            ON_BEEPER -> push(atomicWorld.get().onBeeper())
            BEEPER_AHEAD -> push(atomicWorld.get().beeperAhead())
            LEFT_IS_CLEAR -> push(atomicWorld.get().leftIsClear())
            FRONT_IS_CLEAR -> push(atomicWorld.get().frontIsClear())
            RIGHT_IS_CLEAR -> push(atomicWorld.get().rightIsClear())

            NOT -> push(pop() == 0)
            AND -> push(pop() and pop())
            OR -> push(pop() or pop())
            XOR -> push(pop() xor pop())

            else -> throw IllegalBytecode(bytecode)
        }
        ++pc
    }
}
