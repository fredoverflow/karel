package logic

import logic.World.EAST
import logic.World.NORTH
import logic.World.SOUTH
import logic.World.WEST
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Week1Test : WorldTestBase() {
    @Test
    fun karelsFirstProgram() {
        executeGoal(World.karelsFirstProgram)
        assertKarelAt(4, 8, EAST)
        assertSoleBeeperAt(3, 8)
    }

    @Test
    fun obtainArtifact() {
        executeGoal(World.obtainArtifact)
        assertKarelAt(3, 5, SOUTH)
        assertSoleBeeperAtKarel()
    }

    @Test
    fun defuseOneBomb() {
        executeGoal(World.defuseOneBomb)
        assertKarelAt(0, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun defuseTwoBombs() {
        executeGoal(World.defuseTwoBombs)
        assertKarelAt(0, 9, NORTH)
        assertNoBeepers()
    }

    @Test
    fun practiceHomeRun() {
        executeGoal(World.practiceHomeRun)
        assertKarelAt(0, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun climbTheStairs() {
        executeGoal(World.climbTheStairs)
        assertKarelAt(7, 3, EAST)
        assertNoBeepers()
    }

    @Test
    fun fillTheHoles() {
        executeGoal(World.fillTheHoles)
        assertKarelAt(9, 8, EAST)
        assertNumberOfBeepers(4)
        assertAllBeepersTouch(FloorPlan.WALL_ALL - FloorPlan.WALL_NORTH)
    }

    @Test
    fun saveTheFlower() {
        executeGoal(World.saveTheFlower)
        assertKarelAt(9, 9, EAST)
        assertSoleBeeperAt(5, 1)
    }

    @Test
    fun mowTheLawn() {
        executeGoal(World.mowTheLawn)
        assertKarelAt(1, 2, WEST)
        assertNoBeepers()
    }

    @Test
    fun harvestTheField() {
        executeGoal(World.harvestTheField)
        assertKarelAt(1, 4, SOUTH)
        assertNoBeepers()
    }

    @Test
    fun repairTheStreet() {
        executeGoal(World.repairTheStreet)
        assertKarelAt(9, 8, EAST)
        for (x in 0..9) {
            val isSolid = (karel.floorPlan.wallsAt(x, 9).and(FloorPlan.WALL_NORTH)) != 0
            val isRepaired = karel.beeperAt(x, 9)
            assertTrue(isSolid.xor(isRepaired))
        }
    }

    @Test
    fun cleanTheRoom() {
        executeGoal(World.cleanTheRoom)
        assertKarelAt(0, 0, WEST)
        assertNoBeepers()
    }

    @Test
    fun tileTheFloor() {
        executeGoal(World.tileTheFloor)
        assertKarelAt(4, 5, SOUTH)
        assertNumberOfBeepers(100)
    }

    @Test
    fun stealOlympicFire() {
        executeGoal(World.stealOlympicFire)
        assertKarelAt(9, 9, EAST)
        assertNoBeepers()
    }

    @Test
    fun removeTheTiles() {
        executeGoal(World.removeTheTiles)
        assertEquals(100, initialKarel.countBeepers())
        assertKarelAt(4, 5, SOUTH)
        assertNoBeepers()
    }

    @Test
    fun walkTheLabyrinth() {
        executeGoal(World.walkTheLabyrinth)
        assertSoleBeeperAtKarel()
    }
}
