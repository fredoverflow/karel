package logic

import logic.World.EAST
import logic.World.NORTH
import org.junit.Assert.assertEquals
import org.junit.Test

class Week2Test : WorldTestBase() {
    @Test
    fun hangTheLampions() {
        executeGoal(World.hangTheLampions)
        assertKarelAt(9, 9, EAST)
        assertNumberOfBeepers(10)
        assertAllBeepersTouch(FloorPlan.WALL_NORTH)
    }

    @Test
    fun followTheSeeds() {
        executeGoal(World.followTheSeeds)
        assertKarelAt(9, 9, NORTH)
        assertNoBeepers()
    }

    @Test
    fun cleanTheTunnels() {
        executeGoal(World.cleanTheTunnels)
        assertKarelAt(9, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun increment() {
        executeGoal(World.increment)
        val before = initialKarel.binaryNumber()
        val after = karel.binaryNumber()
        assertEquals((before + 1).and(255), after)
    }

    @Test
    fun decrement() {
        executeGoal(World.decrement)
        val before = initialKarel.binaryNumber()
        val after = karel.binaryNumber()
        assertEquals((before - 1).and(255), after)
    }

    @Test
    fun addSlow() {
        executeGoal(World.addSlow)
        val one = initialKarel.binaryNumber(0)
        val two = initialKarel.binaryNumber(1)
        val sum = karel.binaryNumber(1)
        assertEquals((one + two).and(255), sum)
    }

    @Test
    fun saveTheFlowers() {
        executeGoal(World.saveTheFlowers)
        assertKarelAt(9, 9, EAST)
        assertNumberOfBeepers(4)
        assertAllBeepersTouch(FloorPlan.WALL_SOUTH)
        assertNoBeepersTouch(FloorPlan.WALL_EAST)
    }

    @Test
    fun findTeddyBear() {
        executeGoal(World.findTeddyBear)
        assertSoleBeeperAtKarel()
    }

    @Test
    fun jumpTheHurdles() {
        executeGoal(World.jumpTheHurdles)
        val x = Integer.numberOfTrailingZeros((initialKarel.beepersHi.ushr(9 * 10 - 64)).toInt())
        assertKarelAt(x, 9, EAST)
        assertSoleBeeperAtKarel()
    }

    @Test
    fun solveTheMaze() {
        executeGoal(World.solveTheMaze)
        assertSoleBeeperAtKarel()
    }

    @Test
    fun quantize() {
        executeGoal(World.quantize)
        assertKarelAt(9, 9, EAST)
        for (x in 0..9) {
            val expected = initialKarel.beeperAt(x, 4)
            for (y in 0..9) {
                assertEquals(expected, karel.beeperAt(x, y))
            }
        }
    }

    @Test
    fun addFast() {
        executeGoal(World.addFast)
        val one = initialKarel.binaryNumber(0)
        val two = initialKarel.binaryNumber(1)
        val sum = karel.binaryNumber(3)
        assertEquals((one + two).and(255), sum)
    }
}
