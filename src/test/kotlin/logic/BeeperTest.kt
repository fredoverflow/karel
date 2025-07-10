package logic

import org.junit.Assert.*
import org.junit.Test

class BeeperTest {
    @Test
    fun dropOneBeeper() {
        FloorPlan.empty.world().apply {
            assertFalse(beeperAt(1, 2))
            dropBeeper(1, 2)
            assertTrue(beeperAt(1, 2))
        }
    }

    @Test
    fun dropAnotherBeeper() {
        FloorPlan.empty.world().apply {
            dropBeeper(1, 2)
            assertThrows(CellIsFull::class.java) {
                dropBeeper(1, 2)
            }
        }
    }

    @Test
    fun dropFourCornerBeepers() {
        FloorPlan.empty.world().apply {
            assertEquals(0, countBeepers())
            dropBeeper(0, 0)
            dropBeeper(9, 0)
            dropBeeper(0, 9)
            dropBeeper(9, 9)
            assertEquals(4, countBeepers())
        }
    }

    @Test
    fun pickOneBeeper() {
        World(0, 1, FloorPlan.empty).apply {
            assertTrue(beeperAt(0, 0))
            pickBeeper(0, 0)
            assertFalse(beeperAt(0, 0))
        }
    }

    @Test
    fun pickImaginaryBeeper() {
        FloorPlan.empty.world().apply {
            assertThrows(CellIsEmpty::class.java) {
                pickBeeper(0, 0)
            }
        }
    }
}
