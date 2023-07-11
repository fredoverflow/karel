package logic

enum class CheckAfter(val toolTipText: String) {
    BEEPER_MOVE("check after every beeper/move"),
    BEEPER("check after every beeper"),
    FINISH("check once after finish"),
}
