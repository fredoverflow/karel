package logic

import logic.Problem.Companion.EAST
import logic.Problem.Companion.NORTH
import logic.Problem.Companion.WEST
import org.junit.Assert.*
import org.junit.Test

class Week3Test : WorldTestBase() {
    @Test
    fun partyAgain() {
        executeGoal(Problem.partyAgain)
        assertKarelAt(9, 8, EAST)
        assertNumberOfBeepers(10)
        assertAllBeepersTouch(NORTH)
    }

    @Test
    fun fetchTheStars() {
        executeGoal(Problem.fetchTheStars)
        assertKarelAt(9, 8, EAST)
        assertNumberOfBeepers(10)
        assertNoBeepersTouch(NORTH)
    }

    @Test
    fun secureTheCave() {
        executeGoal(Problem.secureTheCave)
        for (x in 0..9) {
            val n = initialWorld.countBeepersInColumn(x)
            for (y in 0 until 10 - n) {
                assertFalse(world[x, y])
            }
            for (y in 10 - n until 10) {
                assertTrue(world[x, y])
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
