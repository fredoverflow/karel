package logic

// The dynamic state of the system is stored in just two 64-bit longs:
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

data class KarelWorld(private val hi: Long, private val lo: Long, val floorPlan: FloorPlan) {
    val beepersLo: Long
        get() = lo

    val beepersHi: Long
        get() = hi.and(BEEPERS_HI)

    val x: Int
        get() = hi.ushr(X_SHIFT).toInt().and(15)

    val y: Int
        get() = hi.ushr(Y_SHIFT).toInt().and(15)

    val direction: Int
        get() = hi.ushr(D_SHIFT).toInt()

    fun withBeepers(hi: Long, lo: Long): KarelWorld {
        return copy(hi = hi, lo = lo)
    }

    fun withKarelAt(x: Int, y: Int, direction: Int): KarelWorld {
        val coordinates = direction.toLong().shl(D_SHIFT).or(y.toLong().shl(Y_SHIFT)).or(x.toLong().shl(X_SHIFT))
        return copy(hi = hi.and(BEEPERS_HI).or(coordinates))
    }

    // BEEPERS

    private fun beepersAt(input: Long, position: Int): Int {
        return input.ushr(position).toInt().and(1)
    }

    private fun pickBeeper(input: Long, position: Int): Long {
        val output = input.and((1L.shl(position)).inv())
        if (output == input) throw CellIsEmpty()
        return output
    }

    private fun dropBeeper(input: Long, position: Int): Long {
        val output = input.or(1L.shl(position))
        if (output == input) throw CellIsFull()
        return output
    }

    private fun toggleBeeper(input: Long, position: Int): Long {
        return input.xor(1L.shl(position))
    }

    fun beeperAt(x: Int, y: Int): Boolean {
        return beepersAt(x, y) != 0
    }

    fun beepersAt(x: Int, y: Int): Int {
        val index = y * 10 + x
        return if (index >= 64) {
            beepersAt(hi, index - 64)
        } else {
            beepersAt(lo, index)
        }
    }

    fun pickBeeper(x: Int, y: Int): KarelWorld {
        val index = y * 10 + x
        return if (index >= 64) {
            copy(hi = pickBeeper(hi, index - 64))
        } else {
            copy(lo = pickBeeper(lo, index))
        }
    }

    fun dropBeeper(x: Int, y: Int): KarelWorld {
        val index = y * 10 + x
        return if (index >= 64) {
            copy(hi = dropBeeper(hi, index - 64))
        } else {
            copy(lo = dropBeeper(lo, index))
        }
    }

    fun toggleBeeper(x: Int, y: Int): KarelWorld {
        val index = y * 10 + x
        return if (index >= 64) {
            copy(hi = toggleBeeper(hi, index - 64))
        } else {
            copy(lo = toggleBeeper(lo, index))
        }
    }

    fun fillWithBeepers(): KarelWorld {
        return copy(hi = hi.or(BEEPERS_HI), lo = -1)
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
        return (0..9).sumBy { y -> beepersAt(x, y) }
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

    fun moveForward(): KarelWorld {
        if (!frontIsClear()) throw BlockedByWall()
        return copy(hi = (hi + deltaXY[direction]).and(CLEAR_CARRY))
    }

    fun turn(delta: Int): KarelWorld {
        return copy(hi = (hi + (delta.toLong().shl(D_SHIFT))).and(CLEAR_CARRY))
    }

    fun turnLeft(): KarelWorld {
        return turn(1)
    }

    fun turnAround(): KarelWorld {
        return turn(2)
    }

    fun turnRight(): KarelWorld {
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
        return (0 <= x && x < World.WIDTH) && (0 <= y && y < World.HEIGHT)
    }

    fun pickBeeper(): KarelWorld {
        return pickBeeper(x, y)
    }

    fun dropBeeper(): KarelWorld {
        return dropBeeper(x, y)
    }
}
