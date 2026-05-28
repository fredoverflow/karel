package logic

class KnuthRandom {
    private var seed = System.nanoTime()

    fun nextLongBits(bits: Int): Long {
        val next = seed * 6364136223846793005 + 1
        seed = next
        return next ushr (64 - bits)
    }

    fun nextLong(): Long {
        return nextLongBits(64) xor nextLongBits(32)
    }

    fun nextIntBits(bits: Int): Int {
        return nextLongBits(bits).toInt()
    }

    fun nextInt(bound: Int): Int {
        return nextIntBits(31) % bound
    }

    fun shuffle(size: Int): IntArray {
        val a = IntArray(size) { index -> index }
        var n = size
        while (n > 1) {
            val i = nextInt(n--)
            val temp = a[i]
            a[i] = a[n]
            a[n] = temp
        }
        return a
    }
}
