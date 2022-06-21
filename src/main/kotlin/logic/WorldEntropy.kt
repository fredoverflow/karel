package logic

class WorldEntropy(private var entropy: Int) {
    fun nextBoolean(): Boolean {
        val lsb = entropy.and(1)
        entropy = entropy.ushr(1)
        return lsb != 0
    }

    fun nextInt(bound: Int): Int {
        val result = entropy % bound
        entropy /= bound
        return result
    }
}
