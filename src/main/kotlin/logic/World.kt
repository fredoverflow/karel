package logic

class World(val floorPlan: FloorPlan) {

    var position: Int = 0
        private set

    val x: Int
        get() = position % 10

    val y: Int
        get() = position / 10

    var direction: Int = 0
        private set

    fun setKarel(x: Int, y: Int, direction: Int) {
        this.position = y * 10 + x
        this.direction = direction
    }

    var beepersLo: Long = 0
        private set

    var beepersHi: Long = 0
        private set

    fun setBeepers(hi: Long, lo: Long) {
        beepersLo = lo
        beepersHi = hi
    }

    fun clone(): World {
        return World(floorPlan).apply {
            position = this@World.position
            direction = this@World.direction
            beepersLo = this@World.beepersLo
            beepersHi = this@World.beepersHi
        }
    }

    // BEEPERS

    private fun beeperAt(input: Long, shift: Int): Boolean {
        return input ushr shift and 1L != 0L
    }

    private fun pickBeeper(input: Long, shift: Int): Long {
        val output = input and (1L shl shift).inv()
        if (output == input) throw CellIsEmpty
        return output
    }

    private fun dropBeeper(input: Long, shift: Int): Long {
        val output = input or (1L shl shift)
        if (output == input) throw CellIsFull
        return output
    }

    private fun toggleBeeper(input: Long, shift: Int): Long {
        return input xor (1L shl shift)
    }

    fun beeperAt(x: Int, y: Int): Boolean {
        return beeperAt(y * 10 + x)
    }

    fun beeperAt(shift: Int): Boolean {
        return if (shift < 64) {
            beeperAt(beepersLo, shift)
        } else {
            beeperAt(beepersHi, shift)
        }
    }

    fun pickBeeper(x: Int, y: Int) {
        pickBeeper(y * 10 + x)
    }

    fun pickBeeper(shift: Int) {
        if (shift < 64) {
            beepersLo = pickBeeper(beepersLo, shift)
        } else {
            beepersHi = pickBeeper(beepersHi, shift)
        }
    }

    fun dropBeeper(x: Int, y: Int) {
        dropBeeper(y * 10 + x)
    }

    fun dropBeeper(shift: Int) {
        if (shift < 64) {
            beepersLo = dropBeeper(beepersLo, shift)
        } else {
            beepersHi = dropBeeper(beepersHi, shift)
        }
    }

    fun toggleBeeper(x: Int, y: Int) {
        toggleBeeper(y * 10 + x)
    }

    fun toggleBeeper(shift: Int) {
        if (shift < 64) {
            beepersLo = toggleBeeper(beepersLo, shift)
        } else {
            beepersHi = toggleBeeper(beepersHi, shift)
        }
    }

    fun fillWithBeepers() {
        beepersLo = -1
        beepersHi = 0xf_ffff_ffff
    }

    fun countBeepers(): Int {
        return beepersLo.countOneBits() + beepersHi.countOneBits()
    }

    fun firstByte(): Int {
        return beepersLo.reverseByte(2)
    }

    fun secondByte(): Int {
        return beepersLo.reverseByte(12)
    }

    fun thirdByte(): Int {
        return beepersLo.reverseByte(22)
    }

    fun fourthByte(): Int {
        return beepersLo.reverseByte(32)
    }

    fun allBytes(): IntArray {
        return intArrayOf(
            beepersLo.reverseByte(2),
            beepersLo.reverseByte(12),
            beepersLo.reverseByte(22),
            beepersLo.reverseByte(32),
            beepersLo.reverseByte(42),
            beepersLo.reverseByte(52),

            ((beepersLo ushr 62) or (beepersHi shl 2)).reverseByte(0),

            beepersHi.reverseByte(8),
            beepersHi.reverseByte(18),
            beepersHi.reverseByte(28),
        )
    }

    fun countBeepersInColumn(x: Int): Int {
        return (0..9).count { y -> beeperAt(x, y) }
    }

    // KAREL

    fun leftIsClear(): Boolean {
        return floorPlan.isClear(position, direction + 1 and 3)
    }

    fun frontIsClear(): Boolean {
        return floorPlan.isClear(position, direction)
    }

    fun rightIsClear(): Boolean {
        return floorPlan.isClear(position, direction - 1 and 3)
    }

    fun moveForward() {
        if (!frontIsClear()) throw BlockedByWall
        //                -1     1
        position += (0x0a_ff_f6_01 ushr direction * 8).toByte().toInt()
    }   //             10   -10

    fun turnLeft() {
        direction = direction + 1 and 3
    }

    fun turnAround() {
        direction = direction xor 2
    }

    fun turnRight() {
        direction = direction - 1 and 3
    }

    fun onBeeper(): Boolean {
        return beeperAt(position)
    }

    fun beeperAhead(): Boolean = when (direction) {

        0 -> beeperAt(position + 1) && (x < 9)
        1 -> beeperAt(position - 10 and 127)
        2 -> beeperAt(position - 1) && (x > 0)
        3 -> beeperAt(position + 10)

        else -> error(direction)
    }

    fun pickBeeper() {
        pickBeeper(position)
    }

    fun dropBeeper() {
        dropBeeper(position)
    }
}

private fun Long.reverseByte(shift: Int): Int {
    val byte = (this ushr shift).toInt() and 255
    val reverse =
        "\u0000\u0080\u0040\u00c0\u0020\u00a0\u0060\u00e0\u0010\u0090\u0050\u00d0\u0030\u00b0\u0070\u00f0\u0008\u0088\u0048\u00c8\u0028\u00a8\u0068\u00e8\u0018\u0098\u0058\u00d8\u0038\u00b8\u0078\u00f8\u0004\u0084\u0044\u00c4\u0024\u00a4\u0064\u00e4\u0014\u0094\u0054\u00d4\u0034\u00b4\u0074\u00f4\u000c\u008c\u004c\u00cc\u002c\u00ac\u006c\u00ec\u001c\u009c\u005c\u00dc\u003c\u00bc\u007c\u00fc\u0002\u0082\u0042\u00c2\u0022\u00a2\u0062\u00e2\u0012\u0092\u0052\u00d2\u0032\u00b2\u0072\u00f2\u000a\u008a\u004a\u00ca\u002a\u00aa\u006a\u00ea\u001a\u009a\u005a\u00da\u003a\u00ba\u007a\u00fa\u0006\u0086\u0046\u00c6\u0026\u00a6\u0066\u00e6\u0016\u0096\u0056\u00d6\u0036\u00b6\u0076\u00f6\u000e\u008e\u004e\u00ce\u002e\u00ae\u006e\u00ee\u001e\u009e\u005e\u00de\u003e\u00be\u007e\u00fe\u0001\u0081\u0041\u00c1\u0021\u00a1\u0061\u00e1\u0011\u0091\u0051\u00d1\u0031\u00b1\u0071\u00f1\u0009\u0089\u0049\u00c9\u0029\u00a9\u0069\u00e9\u0019\u0099\u0059\u00d9\u0039\u00b9\u0079\u00f9\u0005\u0085\u0045\u00c5\u0025\u00a5\u0065\u00e5\u0015\u0095\u0055\u00d5\u0035\u00b5\u0075\u00f5\u000d\u008d\u004d\u00cd\u002d\u00ad\u006d\u00ed\u001d\u009d\u005d\u00dd\u003d\u00bd\u007d\u00fd\u0003\u0083\u0043\u00c3\u0023\u00a3\u0063\u00e3\u0013\u0093\u0053\u00d3\u0033\u00b3\u0073\u00f3\u000b\u008b\u004b\u00cb\u002b\u00ab\u006b\u00eb\u001b\u009b\u005b\u00db\u003b\u00bb\u007b\u00fb\u0007\u0087\u0047\u00c7\u0027\u00a7\u0067\u00e7\u0017\u0097\u0057\u00d7\u0037\u00b7\u0077\u00f7\u000f\u008f\u004f\u00cf\u002f\u00af\u006f\u00ef\u001f\u009f\u005f\u00df\u003f\u00bf\u007f\u00ff"
    return reverse[byte].code
}
