package logic

import common.Stack
import org.junit.Assert.*
import vm.VirtualMachine

private val requiredInitializer: World = fenced().placeKarel()

open class WorldTestBase : VirtualMachine.Callbacks {
    protected var initialWorld: World = requiredInitializer
    protected var world: World = requiredInitializer

    protected fun executeGoal(problem: Problem) {
        val instructions = vm.createGoalInstructions(problem.goal)
        initialWorld = problem.randomWorld()
        world = initialWorld.copy()
        val virtualMachine = VirtualMachine(instructions, world, this)
        try {
            virtualMachine.executeGoalProgram()
        } catch (_: Stack.Exhausted) {
        }
    }

    override fun onInfiniteLoop() {
        fail("infinite loop detected")
    }

    protected fun assertKarelAt(x: Int, y: Int, direction: Int) {
        assertEquals(x, world.x)
        assertEquals(y, world.y)
        assertEquals(direction, world.direction)
    }

    protected fun assertSoleBeeperAt(x: Int, y: Int) {
        assert(world[x, y])
        assertNumberOfBeepers(1)
    }

    protected fun assertSoleBeeperAtKarel() {
        assertSoleBeeperAt(world.x, world.y)
    }

    protected fun assertNoBeepers() {
        assertNumberOfBeepers(0)
    }

    protected fun assertNumberOfBeepers(expected: Int) {
        val actual = world.countBeepers()
        assertEquals(expected, actual)
    }

    protected fun assertAllBeepersTouch(direction: Int) {
        val delta = delta(direction)
        var cell = CELL_TOP_LEFT
        for (y in 0 until 10) {
            for (x in 0 until 10) {
                if (world[cell]) {
                    assertTrue(world[cell + delta])
                }
                cell += CELL_NEXT_COLUMN
            }
            cell += CELL_NEXT_ROW
        }
    }

    protected fun assertNoBeepersTouch(direction: Int) {
        val delta = delta(direction)
        var cell = CELL_TOP_LEFT
        for (y in 0 until 10) {
            for (x in 0 until 10) {
                if (world[cell]) {
                    assertFalse(world[cell + delta])
                }
                cell += CELL_NEXT_COLUMN
            }
            cell += CELL_NEXT_ROW
        }
    }
}
