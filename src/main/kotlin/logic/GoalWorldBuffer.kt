package logic

class GoalWorldBuffer(size: Int) {
    @Suppress("UNCHECKED_CAST")
    private val worlds = arrayOfNulls<World>(size) as Array<World>
    private var writer = 0
    private var reader = 0

    fun add(world: World) {
        worlds[writer++] = world
    }

    fun hasNext(): Boolean {
        return reader < writer
    }

    fun next(): World {
        return worlds[reader++]
    }

    fun lastOrNull(): World? {
        return if (writer > 0) worlds[writer - 1] else null
    }

    fun last(): World {
        return worlds[writer - 1]
    }
}
