package common

fun <E> List<E>.subList(fromIndex: Int): List<E> {
    return subList(fromIndex, size)
}
