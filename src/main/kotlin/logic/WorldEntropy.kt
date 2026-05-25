package logic

class WorldEntropy(private var entropy: Int) {

    fun nextBoolean(): Boolean {
        val bit = entropy and 1
        entropy = entropy ushr 1
        return bit != 0
    }

    fun nextDirection(): Int {
        val dir = entropy and 3
        entropy = entropy ushr 2
        return dir
    }

    fun nextByte(): Int {
        val byte = entropy and 255
        entropy = entropy ushr 8
        return byte
    }

    fun nextInt(bound: Int): Int {
        val result = entropy % bound
        entropy /= bound
        return result
    }
}
