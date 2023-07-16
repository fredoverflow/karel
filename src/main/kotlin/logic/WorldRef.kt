package logic

class WorldRef(@JvmField var world: World) {

    inline fun updateAndGet(updateFunction: (World) -> World): World {
        val next = updateFunction(world)
        world = next
        return next
    }
}
