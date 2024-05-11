package logic

import logic.Problem.Companion.EAST
import logic.Problem.Companion.NORTH
import logic.Problem.Companion.SOUTH
import logic.Problem.Companion.WEST
import org.junit.Assert.*
import org.junit.Test

class Week3Test : WorldTestBase() {
    @Test
    fun partyAgain() {
        executeGoal(Problem.partyAgain)
        assertKarelAt(9, 8, EAST)
        assertNumberOfBeepers(10)
        assertAllBeepersTouch(FloorPlan.WALL_NORTH)
    }

    @Test
    fun fetchTheStars() {
        executeGoal(Problem.fetchTheStars)
        assertKarelAt(9, 8, EAST)
        assertNumberOfBeepers(10)
        assertNoBeepersTouch(FloorPlan.WALL_NORTH)
    }

    @Test
    fun secureTheCave() {
        executeGoal(Problem.secureTheCave)
        for (x in 0..9) {
            val n = initialWorld.countBeepersInColumn(x)
            for (y in 0 until 10 - n) {
                assertFalse(world.beeperAt(x, y))
            }
            for (y in 10 - n until 10) {
                assertTrue(world.beeperAt(x, y))
            }
        }
    }

    @Test
    fun layAndRemoveTiles() {
        executeGoal(Problem.layAndRemoveTiles)
        assertKarelAt(0, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun findShelters() {
        executeGoal(Problem.findShelters)
        var floodWorld = initialWorld
        val floorPlan = floodWorld.floorPlan

        // mark reachable positions with beepers
        fun floodFill(x: Int, y: Int) {
            if (floodWorld.beeperAt(x, y)) return

            floodWorld = floodWorld.dropBeeper(x, y)

            if (floorPlan.isClear(x, y, EAST)) {
                floodFill(x + 1, y)
            }
            if (floorPlan.isClear(x, y, NORTH)) {
                floodFill(x, y - 1)
            }
            if (floorPlan.isClear(x, y, WEST)) {
                floodFill(x - 1, y)
            }
            if (floorPlan.isClear(x, y, SOUTH)) {
                floodFill(x, y + 1)
            }
        }
        floodFill(world.x, world.y)

        // remove beepers from shelters
        for (y in 0 until 10) {
            for (x in 0 until 10) {
                if (floodWorld.beeperAt(x, y) && floorPlan.numberOfWallsAt(x, y) >= 3) {
                    floodWorld = floodWorld.pickBeeper(x, y)
                }
            }
        }

        assertEquals(floodWorld.beepersHi, world.beepersHi)
        assertEquals(floodWorld.beepersLo, world.beepersLo)
    }

    @Test
    fun addSmart() {
        executeGoal(Problem.addSmart)
        val one = initialWorld.binaryNumber(0)
        val two = initialWorld.binaryNumber(1)
        val sum = world.binaryNumber(2)
        assertEquals((one + two).and(255), sum)
    }

    @Test
    fun computeFibonacci() {
        executeGoal(Problem.computeFibonacci)
        val numbers = (0..9).map(world::binaryNumber)
        for ((a, b, c) in numbers.windowed(3)) {
            assertEquals(a + b, c)
        }
    }
}
