package util

class IdentityGenerator(start: Int = 0) {

    private val identities = HashMap<Any, Int>()
    private var counter = start

    operator fun invoke(entity: Any): Int {
        return identities.getOrPut(entity) { counter++ }
    }
}
