package logic

enum class Check(val singular: String, val plural: String) {
    EVERY_PICK_DROP_MOVE("pick/drop/move", "picks/drops/moves"),
    EVERY_PICK_DROP("pick/drop", "picks/drops");

    fun numerus(n: Int): String = when (n) {
        1 -> singular
        else -> plural
    }
}
