package logic

import logic.Problem.Companion.EAST
import logic.Problem.Companion.NORTH
import logic.Problem.Companion.SOUTH
import logic.Problem.Companion.WEST
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Week1Test : WorldTestBase() {
    @Test
    fun karelsFirstProgram() {
        executeGoal(Problem.karelsFirstProgram)
        assertKarelAt(4, 8, EAST)
        assertSoleBeeperAt(3, 8)
    }

    @Test
    fun obtainArtifact() {
        executeGoal(Problem.obtainArtifact)
        assertKarelAt(3, 5, SOUTH)
        assertSoleBeeperAtKarel()
    }

    @Test
    fun defuseOneBomb() {
        executeGoal(Problem.defuseOneBomb)
        assertKarelAt(0, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun defuseTwoBombs() {
        executeGoal(Problem.defuseTwoBombs)
        assertKarelAt(0, 9, NORTH)
        assertNoBeepers()
    }

    @Test
    fun practiceHomeRun() {
        executeGoal(Problem.practiceHomeRun)
        assertKarelAt(0, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun climbTheStairs() {
        executeGoal(Problem.climbTheStairs)
        assertKarelAt(7, 3, EAST)
        assertNoBeepers()
    }

    @Test
    fun fillTheHoles() {
        executeGoal(Problem.fillTheHoles)
        assertKarelAt(9, 8, EAST)
        assertNumberOfBeepers(4)
        assertAllBeepersTouch(FloorPlan.WALL_ALL - FloorPlan.WALL_NORTH)
    }

    @Test
    fun saveTheFlower() {
        executeGoal(Problem.saveTheFlower)
        assertKarelAt(9, 9, EAST)
        assertSoleBeeperAt(5, 1)
    }

    @Test
    fun mowTheLawn() {
        executeGoal(Problem.mowTheLawn)
        assertKarelAt(1, 2, WEST)
        assertNoBeepers()
    }

    @Test
    fun harvestTheField() {
        executeGoal(Problem.harvestTheField)
        assertKarelAt(2, 4, SOUTH)
        assertNoBeepers()
    }

    @Test
    fun repairTheStreet() {
        executeGoal(Problem.repairTheStreet)
        assertKarelAt(9, 8, EAST)
        for (x in 0..9) {
            val isSolid = (world.floorPlan.wallsAt(x, 9).and(FloorPlan.WALL_NORTH)) != 0
            val isRepaired = world.beeperAt(x, 9)
            assertTrue(isSolid.xor(isRepaired))
        }
    }

    @Test
    fun cleanTheRoom() {
        executeGoal(Problem.cleanTheRoom)
        assertKarelAt(0, 0, WEST)
        assertNoBeepers()
    }

    @Test
    fun tileTheFloor() {
        executeGoal(Problem.tileTheFloor)
        assertKarelAt(4, 5, SOUTH)
        assertNumberOfBeepers(100)
    }

    @Test
    fun stealOlympicFire() {
        executeGoal(Problem.stealOlympicFire)
        assertKarelAt(9, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun removeTheTiles() {
        executeGoal(Problem.removeTheTiles)
        assertEquals(100, initialWorld.countBeepers())
        assertKarelAt(4, 5, SOUTH)
        assertNoBeepers()
    }

    @Test
    fun walkTheLabyrinth() {
        executeGoal(Problem.walkTheLabyrinth)
        assertSoleBeeperAtKarel()
    }
}
