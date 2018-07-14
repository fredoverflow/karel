package logic

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import vm.VirtualMachine
import java.util.concurrent.atomic.AtomicReference

open class WorldTestBase {
    protected var initialKarel: KarelWorld = World.emptyWorld
    protected var karel: KarelWorld = World.emptyWorld

    protected fun executeGoal(problem: Problem) {
        val instructions = vm.instructionBuffer()
        instructions.addAll(problem.goal.map { vm.goalInstruction(it.toInt()) })
        initialKarel = problem.createWorld()
        val atomicKarel = AtomicReference<KarelWorld>(initialKarel)
        val virtualMachine = VirtualMachine(instructions, atomicKarel, this::push, this::pop, this::infiniteLoopDetected)
        try {
            virtualMachine.stepReturn()
        } catch (error: AssertionError) {
            // TODO Does Kotlin have exception filters/guards?
            if (!error.message!!.contains("empty")) throw error
            // The final RET instruction tried to pop off the empty stack.
        }
        karel = atomicKarel.get()
    }

    private fun push(callerPosition: Int, calleePosition: Int) {
    }

    private fun pop() {
    }

    private fun infiniteLoopDetected() {
        fail("infinite loop detected")
    }

    protected fun assertKarelAt(x: Int, y: Int, direction: Int) {
        assertEquals(x, karel.x)
        assertEquals(y, karel.y)
        assertEquals(direction, karel.direction)
    }

    protected fun assertSoleBeeperAt(x: Int, y: Int) {
        val expected = World.emptyWorld.dropBeeper(x, y)
        assertEquals(expected.beepersHi, karel.beepersHi)
        assertEquals(expected.beepersLo, karel.beepersLo)
    }

    protected fun assertSoleBeeperAtKarel() {
        assertSoleBeeperAt(karel.x, karel.y)
    }

    protected fun assertNoBeepers() {
        assertEquals(0, karel.beepersHi)
        assertEquals(0, karel.beepersLo)
    }

    protected fun assertNumberOfBeepers(expected: Int) {
        val actual = karel.countBeepers()
        assertEquals(expected, actual)
    }

    protected fun assertAllBeepersTouch(walls: Int) {
        for (y in 0 until World.HEIGHT) {
            for (x in 0 until World.WIDTH) {
                if (karel.beeperAt(x, y)) {
                    assertEquals(walls, karel.floorPlan.wallsAt(x, y).and(walls))
                }
            }
        }
    }

    protected fun assertNoBeepersTouch(walls: Int) {
        for (y in 0 until World.HEIGHT) {
            for (x in 0 until World.WIDTH) {
                if (karel.beeperAt(x, y)) {
                    assertEquals(FloorPlan.WALL_NONE, karel.floorPlan.wallsAt(x, y).and(walls))
                }
            }
        }
    }
}
