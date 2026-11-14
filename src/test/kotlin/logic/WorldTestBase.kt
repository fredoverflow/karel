package logic

import org.junit.jupiter.api.Assertions.assertEquals
import vm.VirtualMachine

abstract class WorldTestBase {

    protected var initialWorld: World = FloorPlan.empty.world()
    protected var world: World = FloorPlan.empty.world()

    protected fun executeGoal(problem: Problem) {
        initialWorld = problem.randomWorld()
        val virtualMachine = VirtualMachine(problem.goal, initialWorld.clone())
        try {
            virtualMachine.executeGoal()
        } catch (_: VirtualMachine.Finished) {
        }
        world = virtualMachine.world
    }

    protected fun assertKarelAt(x: Int, y: Int, direction: Int) {
        assertEquals(x, world.x)
        assertEquals(y, world.y)
        assertEquals(direction, world.direction)
    }

    protected fun assertSoleBeeperAt(x: Int, y: Int) {
        FloorPlan.empty.world().apply {
            dropBeeper(x, y)
            assertEquals(beepersLo, world.beepersLo)
            assertEquals(beepersHi, world.beepersHi)
        }
    }

    protected fun assertSoleBeeperAtKarel() {
        assertSoleBeeperAt(world.x, world.y)
    }

    protected fun assertNoBeepers() {
        assertEquals(0, world.beepersLo)
        assertEquals(0, world.beepersHi)
    }

    protected fun assertNumberOfBeepers(expected: Int) {
        val actual = world.countBeepers()
        assertEquals(expected, actual)
    }

    protected fun assertAllBeepersTouch(walls: Int) {
        for (position in 0 until 100) {
            if (world.beeperAt(position)) {
                assertEquals(walls, world.floorPlan.wallsAt(position) and walls)
            }
        }
    }

    protected fun assertNoBeepersTouch(walls: Int) {
        for (position in 0 until 100) {
            if (world.beeperAt(position)) {
                assertEquals(0, world.floorPlan.wallsAt(position) and walls)
            }
        }
    }
}
