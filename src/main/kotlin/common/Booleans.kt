package common

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

fun Boolean.toInt(one: Int): Int {
    return if (this) one else 0
}
