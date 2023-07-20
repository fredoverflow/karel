package logic

import org.junit.Assert.*
import org.junit.Test

class BeeperTest {
    @Test
    fun dropOneBeeper() {
        val world = fenced().placeKarel()
        assertFalse(world.onBeeper())

        world.dropBeeper()
        assertTrue(world.onBeeper())
    }

    @Test
    fun dropAnotherBeeper() {
        val world = fenced().placeKarel()
        world.dropBeeper()

        assertThrows(CellIsFull::class.java) {
            world.dropBeeper()
        }
    }

    @Test
    fun dropFourCornerBeepers() {
        val world = fenced()
            .drop(0, 0)
            .drop(9, 0)
            .drop(0, 9)
            .drop(9, 9)
            .placeKarel()

        assertEquals(4, world.countBeepers())
    }

    @Test
    fun pickOneBeeper() {
        val world = fenced()
            .drop(0, 0)
            .placeKarel(0, 0)

        assertTrue(world.onBeeper())
        world.pickBeeper()
        assertFalse(world.onBeeper())
    }

    @Test
    fun pickImaginaryBeeper() {
        assertThrows(CellIsEmpty::class.java) {
            fenced()
                .placeKarel()
                .pickBeeper()
        }
    }
}
