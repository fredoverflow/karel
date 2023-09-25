package vm

sealed class Stack(@JvmField val head: Int, @JvmField val tail: Stack?) {

    class ReturnAddress(head: Int, tail: Stack?) : Stack(head, tail)

    class LoopCounter(head: Int, tail: Stack?) : Stack(head, tail)

    class Boolean(head: Int, tail: Stack?) : Stack(head, tail)

    @JvmField
    val size: Int = tail.size + 1
}

val Stack?.size: Int
    get() = this?.size ?: 0

inline fun Stack?.forEach(action: (Stack) -> Unit) {
    var stack = this
    while (stack != null) {
        action(stack)
        stack = stack.tail
    }
}
