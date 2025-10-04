package logic

import logic.Problem.Companion.EAST
import logic.Problem.Companion.NORTH
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Week2Test : WorldTestBase() {
    @Test
    fun hangTheLampions() {
        executeGoal(Problem.hangTheLampions)
        assertKarelAt(9, 9, EAST)
        assertNumberOfBeepers(10)
        assertAllBeepersTouch(FloorPlan.WALL_NORTH)
    }

    @Test
    fun followTheSeeds() {
        executeGoal(Problem.followTheSeeds)
        assertKarelAt(9, 9, NORTH)
        assertNoBeepers()
    }

    @Test
    fun cleanTheTunnels() {
        executeGoal(Problem.cleanTheTunnels)
        assertKarelAt(9, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun increment() {
        executeGoal(Problem.increment)
        val before = initialWorld.firstByte()
        val after = world.firstByte()
        assertEquals((before + 1).and(255), after)
    }

    @Test
    fun decrement() {
        executeGoal(Problem.decrement)
        val before = initialWorld.firstByte()
        val after = world.firstByte()
        assertEquals((before - 1).and(255), after)
    }

    @Test
    fun addSlow() {
        executeGoal(Problem.addSlow)
        val one = initialWorld.firstByte()
        val two = initialWorld.secondByte()
        val sum = world.secondByte()
        assertEquals((one + two).and(255), sum)
    }

    @Test
    fun saveTheFlowers() {
        executeGoal(Problem.saveTheFlowers)
        assertKarelAt(9, 9, EAST)
        assertNumberOfBeepers(4)
        assertAllBeepersTouch(FloorPlan.WALL_SOUTH)
        assertNoBeepersTouch(FloorPlan.WALL_EAST)
    }

    @Test
    fun findTeddyBear() {
        executeGoal(Problem.findTeddyBear)
        assertSoleBeeperAtKarel()
    }

    @Test
    fun jumpTheHurdles() {
        executeGoal(Problem.jumpTheHurdles)
        val x = Integer.numberOfTrailingZeros((initialWorld.beepersHi.ushr(9 * 10 - 64)).toInt())
        assertKarelAt(x, 9, EAST)
        assertSoleBeeperAtKarel()
    }

    @Test
    fun solveTheMaze() {
        executeGoal(Problem.solveTheMaze)
        assertSoleBeeperAtKarel()
    }

    @Test
    fun quantize() {
        executeGoal(Problem.quantizeBits)
        assertKarelAt(9, 9, EAST)
        for (x in 0..9) {
            val expected = initialWorld.beeperAt(x, 4)
            for (y in 0..9) {
                assertEquals(expected, world.beeperAt(x, y))
            }
        }
    }

    @Test
    fun addFast() {
        executeGoal(Problem.addFast)
        val one = initialWorld.firstByte()
        val two = initialWorld.secondByte()
        val sum = world.fourthByte()
        assertEquals((one + two).and(255), sum)
    }
}
