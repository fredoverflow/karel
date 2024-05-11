package logic

// The state of the world is stored in just two 64-bit longs:
//
//       56       48       40       32       24       16        8        0
// .....cdd ...cyyyy ...cxxxx ....bbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb   hi
// bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb   lo
//
// c : carry
// d : direction
// y : y position
// x : x position
// b : beepers

private const val X_SHIFT = 40
private const val Y_SHIFT = 48
private const val D_SHIFT = 56

private const val BEEPERS_HI = 0x0000000fffffffffL
private const val CLEAR_CARRY = 0x030f0f0fffffffffL

//                              E  N   W  S
private val deltaX = intArrayOf(1, 0, -1, 0)
private val deltaY = intArrayOf(0, -1, 0, 1)
private val deltaXY = longArrayOf(1L.shl(X_SHIFT), 15L.shl(Y_SHIFT), 15L.shl(X_SHIFT), 1L.shl(Y_SHIFT))

class World(private val hi: Long, private val lo: Long, val floorPlan: FloorPlan) {
    val beepersLo: Long
        get() = lo

    val beepersHi: Long
        get() = hi.and(BEEPERS_HI)

    val x: Int
        get() = hi.ushr(X_SHIFT).toInt().and(15)

    val y: Int
        get() = hi.ushr(Y_SHIFT).toInt().and(15)

    fun equalsIgnoringDirection(that: World): Boolean {
        return this.lo == that.lo && this.hi.shl(8) == that.hi.shl(8)
    }

    val direction: Int
        get() = hi.ushr(D_SHIFT).toInt()

    fun withBeepers(hi: Long, lo: Long): World {
        return World(hi, lo, floorPlan)
    }

    fun withKarelAt(x: Int, y: Int, direction: Int): World {
        val coordinates = direction.toLong().shl(D_SHIFT) or y.toLong().shl(Y_SHIFT) or x.toLong().shl(X_SHIFT)
        return World(hi.and(BEEPERS_HI).or(coordinates), lo, floorPlan)
    }

    // BEEPERS

    private fun beepersAt(input: Long, shift: Int): Int {
        return input.ushr(shift).toInt().and(1)
    }

    private fun pickBeeper(input: Long, shift: Int): Long {
        val output = input.and(1L.shl(shift).inv())
        if (output == input) throw CellIsEmpty()
        return output
    }

    private fun dropBeeper(input: Long, shift: Int): Long {
        val output = input.or(1L.shl(shift))
        if (output == input) throw CellIsFull()
        return output
    }

    private fun toggleBeeper(input: Long, shift: Int): Long {
        return input.xor(1L.shl(shift))
    }

    fun beeperAt(x: Int, y: Int): Boolean {
        return beepersAt(x, y) != 0
    }

    fun beepersAt(x: Int, y: Int): Int {
        val shift = y * 10 + x
        return if (shift >= 64) {
            beepersAt(hi, shift)
        } else {
            beepersAt(lo, shift)
        }
    }

    fun pickBeeper(x: Int, y: Int): World {
        val shift = y * 10 + x
        return if (shift >= 64) {
            World(pickBeeper(hi, shift), lo, floorPlan)
        } else {
            World(hi, pickBeeper(lo, shift), floorPlan)
        }
    }

    fun dropBeeper(x: Int, y: Int): World {
        val shift = y * 10 + x
        return if (shift >= 64) {
            World(dropBeeper(hi, shift), lo, floorPlan)
        } else {
            World(hi, dropBeeper(lo, shift), floorPlan)
        }
    }

    fun toggleBeeper(x: Int, y: Int): World {
        val shift = y * 10 + x
        return if (shift >= 64) {
            World(toggleBeeper(hi, shift), lo, floorPlan)
        } else {
            World(hi, toggleBeeper(lo, shift), floorPlan)
        }
    }

    fun fillWithBeepers(): World {
        return World(hi.or(BEEPERS_HI), -1, floorPlan)
    }

    fun countBeepers(): Int {
        val a = Integer.bitCount(beepersHi.ushr(32).toInt())
        val b = Integer.bitCount(hi.toInt())
        val c = Integer.bitCount(lo.ushr(32).toInt())
        val d = Integer.bitCount(lo.toInt())
        return a + b + c + d
    }

    fun binaryNumber(y: Int = 0): Int {
        val bit0 = beepersAt(9, y)
        val bit1 = beepersAt(8, y).shl(1)
        val bit2 = beepersAt(7, y).shl(2)
        val bit3 = beepersAt(6, y).shl(3)
        val bit4 = beepersAt(5, y).shl(4)
        val bit5 = beepersAt(4, y).shl(5)
        val bit6 = beepersAt(3, y).shl(6)
        val bit7 = beepersAt(2, y).shl(7)
        return bit7.or(bit6).or(bit5).or(bit4).or(bit3).or(bit2).or(bit1).or(bit0)
    }

    fun countBeepersInColumn(x: Int): Int {
        return (0..9).sumOf { y -> beepersAt(x, y) }
    }

    // KAREL

    fun leftIsClear(): Boolean {
        return floorPlan.isClear(x, y, (direction + 1).and(3))
    }

    fun frontIsClear(): Boolean {
        return floorPlan.isClear(x, y, direction)
    }

    fun rightIsClear(): Boolean {
        return floorPlan.isClear(x, y, (direction + 3).and(3))
    }

    fun moveForward(): World {
        if (!frontIsClear()) throw BlockedByWall()
        return World((hi + deltaXY[direction]).and(CLEAR_CARRY), lo, floorPlan)
    }

    fun moveBackward(): World {
        if (!floorPlan.isClear(x, y, direction xor 2)) throw BlockedByWall()
        return World((hi + deltaXY[direction xor 2]).and(CLEAR_CARRY), lo, floorPlan)
    }

    fun turn(delta: Int): World {
        return World((hi + delta.toLong().shl(D_SHIFT)).and(CLEAR_CARRY), lo, floorPlan)
    }

    fun turnLeft(): World {
        return turn(1)
    }

    fun turnAround(): World {
        return World(hi xor 2L.shl(D_SHIFT), lo, floorPlan)
    }

    fun turnRight(): World {
        return turn(3)
    }

    fun onBeeper(): Boolean {
        return beeperAt(x, y)
    }

    fun beeperAhead(): Boolean {
        val x = this.x + deltaX[direction]
        val y = this.y + deltaY[direction]
        return isInsideWorld(x, y) && beeperAt(x, y)
    }

    fun isInsideWorld(x: Int, y: Int): Boolean {
        return (0 <= x && x < Problem.WIDTH) && (0 <= y && y < Problem.HEIGHT)
    }

    fun pickBeeper(): World {
        return pickBeeper(x, y)
    }

    fun dropBeeper(): World {
        return dropBeeper(x, y)
    }
}
