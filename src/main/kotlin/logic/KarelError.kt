package logic

abstract class KarelError(message: String) : Exception(message)

class CellIsEmpty : KarelError("there is no beeper to pick")

class CellIsFull : KarelError("cannot drop another beeper")

class BlockedByWall : KarelError("cannot move through wall")
