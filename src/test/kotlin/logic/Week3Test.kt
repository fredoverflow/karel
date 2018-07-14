package logic

import logic.Problem.Companion.EAST
import logic.Problem.Companion.WEST
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
}
