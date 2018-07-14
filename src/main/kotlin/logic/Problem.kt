package logic

class Problem(val index: String, val name: String, val story: String, val goal: String, val binaryLines: Int, val createWorld: () -> KarelWorld) {
    val level: Int
        get() = index[0] - '0'

    override fun toString(): String = "$index $name"
}
