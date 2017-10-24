package util

sealed class Stack<out T> {
    abstract fun isEmpty(): Boolean

    abstract fun top(): T

    abstract fun pop(): Stack<T>

    object Nil : Stack<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun top(): Nothing = throw AssertionError("top on empty stack")

        override fun pop(): Nothing = throw AssertionError("pop on empty stack")
    }

    class Cons<out T>(private val head: T, private val tail: Stack<T>) : Stack<T>() {
        override fun isEmpty(): Boolean = false

        override fun top(): T = head

        override fun pop(): Stack<T> = tail
    }

    inline fun forEach(action: (T) -> Unit) {
        var current = this
        while (!current.isEmpty()) {
            action(current.top())
            current = current.pop()
        }
    }

    fun size(): Int {
        var counter = 0
        forEach { ++counter }
        return counter
    }

    fun drop(n: Int): Stack<T> {
        var current = this
        var i = 0
        while (i < n) {
            current = current.pop()
            ++i
        }
        return current
    }

    operator fun get(index: Int): T {
        return drop(index).top()
    }
}

fun <U, T : U> Stack<T>.push(x: U): Stack<U> = Stack.Cons(x, this)
