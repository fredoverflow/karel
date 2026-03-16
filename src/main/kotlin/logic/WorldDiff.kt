package logic

class WorldDiff(old: World, new: World, binaryLines: Int) {

    // bounding box for changes
    var left: Int private set
    var right: Int private set
    var top: Int private set
    var bottom: Int private set

    init {
        // Which beepers changed?
        val loDiff = old.beepersLo xor new.beepersLo
        val hiDiff = old.beepersHi xor new.beepersHi

        if (loDiff or hiDiff == 0L) {
            // no beepers changed
            left = 9
            right = 0
            top = 9
            bottom = 0
        } else {
            val first = if (loDiff != 0L) {
                loDiff.countTrailingZeroBits()
            } else {
                hiDiff.countTrailingZeroBits() + 64
            }

            val last = if (hiDiff != 0L) {
                127 - hiDiff.countLeadingZeroBits()
            } else {
                63 - loDiff.countLeadingZeroBits()
            }

            top = first / 10
            bottom = last / 10

            if (top == bottom) {
                // beepers changed on same line
                left = if (binaryLines ushr top and 1 != 0) {
                    0
                } else {
                    first % 10
                }
                right = last % 10
            } else {
                // beepers changed on multiple lines
                left = 0
                right = 9
            }
        }

        // Did Karel change?
        if (old.karel != new.karel) {
            // Karel turned or moved
            includeX(old.x)
            includeY(old.y)

            if (new.position != old.position) {
                // Karel moved
                includeX(new.x)
                includeY(new.y)
            }
        }
    }

    private fun includeX(x: Int) {
        if (x < left) left = x
        if (x > right) right = x
    }

    private fun includeY(y: Int) {
        if (y < top) top = y
        if (y > bottom) bottom = y
    }

    fun isEmpty(): Boolean {
        return top > bottom
    }

    fun width(): Int {
        return right - left + 1
    }

    fun height(): Int {
        return bottom - top + 1
    }
}
