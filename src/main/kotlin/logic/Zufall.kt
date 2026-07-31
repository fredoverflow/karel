package logic

class Zufall {
    private var seed = System.nanoTime()

    private fun next(): Long {
        val next = seed * 6364136223846793005 + 1
        seed = next
        return next
    }

    fun nextLong68719476736(): Long {
        return next() ushr -36
    }

    fun nextLong(): Long {
        return next() ushr 32 xor next()
    }

    fun nextInt4(): Int {
        return (next() ushr -2).toInt()
    }

    fun nextInt65536(): Int {
        return (next() ushr -16).toInt()
    }

    fun nextInt(bound: Int): Int {
        return ((next() ushr 16) * bound ushr -16).toInt()
    }
}
