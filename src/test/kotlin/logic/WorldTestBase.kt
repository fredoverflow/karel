package logic

import common.Stack
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import vm.VirtualMachine

open class WorldTestBase : VirtualMachine.Callbacks {
    protected var initialWorld: World = Problem.emptyWorld
    protected var world: World = Problem.emptyWorld

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
        assertEquals(1, world.countBeepers())
    }

    protected fun assertSoleBeeperAtKarel() {
        assertSoleBeeperAt(world.x, world.y)
    }

    protected fun assertNoBeepers() {
        assertEquals(0, world.countBeepers())
    }

    protected fun assertNumberOfBeepers(expected: Int) {
        val actual = world.countBeepers()
        assertEquals(expected, actual)
    }

    protected fun assertAllBeepersTouch(walls: Int) {
        for (y in 0 until Problem.HEIGHT) {
            for (x in 0 until Problem.WIDTH) {
                if (world[x, y]) {
                    TODO() // assertEquals(walls, world.floorPlan.wallsAt(x, y).and(walls))
                }
            }
        }
    }

    protected fun assertNoBeepersTouch(walls: Int) {
        for (y in 0 until Problem.HEIGHT) {
            for (x in 0 until Problem.WIDTH) {
                if (world[x, y]) {
                    TODO() // assertEquals(FloorPlan.WALL_NONE, world.floorPlan.wallsAt(x, y).and(walls))
                }
            }
        }
    }
}
