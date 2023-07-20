package logic

import org.junit.Assert.assertEquals
import org.junit.Test

class GridTest {
    @Test
    fun wall() {
        assertEquals(WALL_TOP_LEFT, wall(0, 0))
        assertEquals(WALL_TOP_RIGHT, wall(10, 0))
        assertEquals(WALL_BOTTOM_LEFT, wall(0, 10))
        assertEquals(WALL_BOTTOM_RIGHT, wall(10, 10))
    }

    @Test
    fun cell() {
        assertEquals(CELL_TOP_LEFT, cell(0, 0))
        assertEquals(CELL_TOP_RIGHT, cell(9, 0))
        assertEquals(CELL_BOTTOM_LEFT, cell(0, 9))
        assertEquals(CELL_BOTTOM_RIGHT, cell(9, 9))
    }

    @Test
    fun delta() {
        assertEquals(EAST, delta(0))
        assertEquals(NORTH, delta(1))
        assertEquals(WEST, delta(2))
        assertEquals(SOUTH, delta(3))

        assertEquals(EAST, delta(4))
        assertEquals(NORTH, delta(5))
        assertEquals(WEST, delta(6))
        assertEquals(SOUTH, delta(7))
    }
}
