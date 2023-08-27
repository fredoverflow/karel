package common

import java.util.function.IntConsumer

class IntArrayBuffer(capacity: Int) : IntConsumer {
    private var data = IntArray(capacity)
    private var size = 0
    private var index = 0

    override fun accept(value: Int) {
        data[size++] = value
    }

    fun hasNext(): Boolean {
        return index < size
    }

    fun next(): Int {
        return data[index++]
    }
}
