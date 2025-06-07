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
        assertKarelAt(0, 9, WEST)
        assertNoBeepers()
    }

    @Test
    fun findShelters() {
        executeGoal(Problem.findShelters)
        var floodWorld = initialWorld
        val floorPlan = floodWorld.floorPlan

        // mark reachable positions with beepers
        fun floodFill(position: Int) {
            if (floodWorld.beeperAt(position)) return

            floodWorld = floodWorld.dropBeeper(position)

            if (floorPlan.isClear(position, EAST)) {
                floodFill(position + 1)
            }
            if (floorPlan.isClear(position, NORTH)) {
                floodFill(position - 10)
            }
            if (floorPlan.isClear(position, WEST)) {
                floodFill(position - 1)
            }
            if (floorPlan.isClear(position, SOUTH)) {
                floodFill(position + 10)
            }
        }
        floodFill(world.position)

        // remove beepers from shelters
        for (position in 0 until 100) {
            if (floodWorld.beeperAt(position) && floorPlan.numberOfWallsAt(position) >= 3) {
                floodWorld = floodWorld.pickBeeper(position)
            }
        }

        assertEquals(floodWorld.beepersHi, world.beepersHi)
        assertEquals(floodWorld.beepersLo, world.beepersLo)
    }

    @Test
    fun addSmart() {
        executeGoal(Problem.addSmart)
        val one = initialWorld.firstByte()
        val two = initialWorld.secondByte()
        val sum = world.thirdByte()
        assertEquals((one + two).and(255), sum)
    }

    @Test
    fun computeFibonacci() {
        executeGoal(Problem.computeFibonacci)
        val bytes = world.allBytes()
        for (i in 2..9) {
            assertEquals(bytes[i - 2] + bytes[i - 1], bytes[i])
        }
    }
}
