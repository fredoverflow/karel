package vm

class ByteArrayClassLoader : ClassLoader() {
    fun defineClass(name: String, bytes: ByteArray): Class<*> {
        return defineClass(name, bytes, 0, bytes.size)
    }
}
