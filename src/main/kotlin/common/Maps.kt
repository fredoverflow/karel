package common

fun <K, V, M : MutableMap<K, V>> M.puts(keys: Array<out K>, value: V): M {
    for (key in keys) {
        put(key, value)
    }
    return this
}

fun <K, V, M : MutableMap<K, V>> M.puts(k1: K, k2: K, value: V): M {
    put(k1, value)
    put(k2, value)
    return this
}

fun <K, V, M : MutableMap<K, V>> M.puts(k1: K, k2: K, k3: K, k4: K, value: V): M {
    put(k1, value)
    put(k2, value)
    put(k3, value)
    put(k4, value)
    return this
}
