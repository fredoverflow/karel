package logic

// The state of the world is stored in just two 64-bit longs:
//
//       56       48       40       32       24       16        8        0
// ........ ...cyyyy ...cxxxx .cddbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb   hi
// bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb bbbbbbbb   lo
//
// c : carry
// y : y position
// x : x position
// d : direction
// b : beepers

private const val D_SHIFT = 36
private const val X_SHIFT = 40
private const val Y_SHIFT = 48

private const val BEEPERS_HI = 0x0000000fffffffffL
private const val CLEAR_CARRY = 0x000f0f3fffffffffL
private const val IGNORING_DIRECTION = 3L.shl(D_SHIFT).inv()

//                              E  N   W  S
private val deltaX = intArrayOf(1, 0, -1, 0)
private val deltaY = intArrayOf(0, -1, 0, 1)
private val deltaXY = longArrayOf(
    1L.shl(X_SHIFT),
    15L.shl(Y_SHIFT),
    15L.shl(X_SHIFT),
    1L.shl(Y_SHIFT),
)

class World(private val hi: Long, private val lo: Long, val floorPlan: FloorPlan) {
    val beepersLo: Long
        get() = lo

    val beepersHi: Long
        get() = hi.and(BEEPERS_HI)

    val direction: Int
        get() = hi.ushr(D_SHIFT).toInt().and(3)

    val x: Int
        get() = hi.ushr(X_SHIFT).toInt().and(15)

    val y: Int
        get() = hi.ushr(Y_SHIFT).toInt().and(15)

    fun equalsIgnoringDirection(that: World): Boolean {
        return this.lo == that.lo && this.hi.and(IGNORING_DIRECTION) == that.hi.and(IGNORING_DIRECTION)
    }

    fun withBeepers(hi: Long, lo: Long): World {
        return World(hi, lo, floorPlan)
    }

    fun withKarelAt(x: Int, y: Int, direction: Int): World {
        val coordinates = y.toLong().shl(Y_SHIFT) or
                x.toLong().shl(X_SHIFT) or
                direction.toLong().shl(D_SHIFT)
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
        return beepersHi.countOneBits() + beepersLo.countOneBits()
    }

    fun firstByte(): Int {
        return lo.reverseByte(2)
    }

    fun secondByte(): Int {
        return lo.reverseByte(12)
    }

    fun thirdByte(): Int {
        return lo.reverseByte(22)
    }

    fun fourthByte(): Int {
        return lo.reverseByte(32)
    }

    fun allBytes(): IntArray {
        return intArrayOf(
            lo.reverseByte(2),
            lo.reverseByte(12),
            lo.reverseByte(22),
            lo.reverseByte(32),
            lo.reverseByte(42),
            lo.reverseByte(52),

            (lo.ushr(62) or hi.shl(2)).reverseByte(0),

            hi.reverseByte(8),
            hi.reverseByte(18),
            hi.reverseByte(28),
        )
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

    fun turn(delta: Int): World {
        return World((hi + delta.toLong().shl(D_SHIFT)).and(CLEAR_CARRY), lo, floorPlan)
    }

    fun turnLeft(): World {
        return turn(1)
    }

    fun turnAround(): World {
        return turn(2)
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
        return (0 <= x && x < 10) && (0 <= y && y < 10)
    }

    fun pickBeeper(): World {
        return pickBeeper(x, y)
    }

    fun dropBeeper(): World {
        return dropBeeper(x, y)
    }
}

private fun Long.reverseByte(shift: Int): Int {
    val byte = this.ushr(shift).toInt().and(255)
    val reverse =
        "\u0000\u0080\u0040\u00c0\u0020\u00a0\u0060\u00e0\u0010\u0090\u0050\u00d0\u0030\u00b0\u0070\u00f0\u0008\u0088\u0048\u00c8\u0028\u00a8\u0068\u00e8\u0018\u0098\u0058\u00d8\u0038\u00b8\u0078\u00f8\u0004\u0084\u0044\u00c4\u0024\u00a4\u0064\u00e4\u0014\u0094\u0054\u00d4\u0034\u00b4\u0074\u00f4\u000c\u008c\u004c\u00cc\u002c\u00ac\u006c\u00ec\u001c\u009c\u005c\u00dc\u003c\u00bc\u007c\u00fc\u0002\u0082\u0042\u00c2\u0022\u00a2\u0062\u00e2\u0012\u0092\u0052\u00d2\u0032\u00b2\u0072\u00f2\u000a\u008a\u004a\u00ca\u002a\u00aa\u006a\u00ea\u001a\u009a\u005a\u00da\u003a\u00ba\u007a\u00fa\u0006\u0086\u0046\u00c6\u0026\u00a6\u0066\u00e6\u0016\u0096\u0056\u00d6\u0036\u00b6\u0076\u00f6\u000e\u008e\u004e\u00ce\u002e\u00ae\u006e\u00ee\u001e\u009e\u005e\u00de\u003e\u00be\u007e\u00fe\u0001\u0081\u0041\u00c1\u0021\u00a1\u0061\u00e1\u0011\u0091\u0051\u00d1\u0031\u00b1\u0071\u00f1\u0009\u0089\u0049\u00c9\u0029\u00a9\u0069\u00e9\u0019\u0099\u0059\u00d9\u0039\u00b9\u0079\u00f9\u0005\u0085\u0045\u00c5\u0025\u00a5\u0065\u00e5\u0015\u0095\u0055\u00d5\u0035\u00b5\u0075\u00f5\u000d\u008d\u004d\u00cd\u002d\u00ad\u006d\u00ed\u001d\u009d\u005d\u00dd\u003d\u00bd\u007d\u00fd\u0003\u0083\u0043\u00c3\u0023\u00a3\u0063\u00e3\u0013\u0093\u0053\u00d3\u0033\u00b3\u0073\u00f3\u000b\u008b\u004b\u00cb\u002b\u00ab\u006b\u00eb\u001b\u009b\u005b\u00db\u003b\u00bb\u007b\u00fb\u0007\u0087\u0047\u00c7\u0027\u00a7\u0067\u00e7\u0017\u0097\u0057\u00d7\u0037\u00b7\u0077\u00f7\u000f\u008f\u004f\u00cf\u002f\u00af\u006f\u00ef\u001f\u009f\u005f\u00df\u003f\u00bf\u007f\u00ff"
    return reverse[byte].code
}
