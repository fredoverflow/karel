package logic

// Some problems use beepers to encode binary numbers, one number per line.
// This function determines how many binary numbers to display, starting from the top.
fun binaryLinesIn(problemName: String): Int {
    return when (problemName) {
        "increment" -> 1
        "decrement" -> 1
        "addSlow" -> 2
        "addFast" -> 4
        else -> 0
    }
}
