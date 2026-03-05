package logic

abstract class KarelError(override val message: String) : Exception(message)

@Suppress("ObjectInheritsException")
object CellIsEmpty : KarelError("there is no beeper to pick") {
    private fun readResolve(): Any = CellIsEmpty
}

@Suppress("ObjectInheritsException")
object CellIsFull : KarelError("cannot drop another beeper") {
    private fun readResolve(): Any = CellIsFull
}

@Suppress("ObjectInheritsException")
object BlockedByWall : KarelError("cannot move through wall") {
    private fun readResolve(): Any = BlockedByWall
}

@Suppress("ObjectInheritsException")
object Weltschmerz : KarelError("weltschmerz") {
    private fun readResolve(): Any = Weltschmerz
}
