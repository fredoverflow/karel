package vm

import logic.World
import java.util.concurrent.atomic.AtomicReference

@Suppress("unused")
abstract class Karel(
    private val atomicWorld: AtomicReference<World>,
    @JvmField protected val callbacks: Callbacks
) {
    interface Callbacks {
        fun pauseAt(position: Int) {}
        fun update() {}

        fun enter(callerPosition: Int, calleePosition: Int) {}
        fun leave() {}
    }

    protected fun moveForward(callerPosition: Int) {
        callbacks.pauseAt(callerPosition)
        atomicWorld.updateAndGet(World::moveForward)
        callbacks.update()
    }

    protected fun turnLeft(callerPosition: Int) {
        callbacks.pauseAt(callerPosition)
        atomicWorld.updateAndGet(World::turnLeft)
        callbacks.update()
    }

    protected fun turnAround(callerPosition: Int) {
        callbacks.pauseAt(callerPosition)
        atomicWorld.updateAndGet(World::turnAround)
        callbacks.update()
    }

    protected fun turnRight(callerPosition: Int) {
        callbacks.pauseAt(callerPosition)
        atomicWorld.updateAndGet(World::turnRight)
        callbacks.update()
    }

    protected fun pickBeeper(callerPosition: Int) {
        callbacks.pauseAt(callerPosition)
        atomicWorld.updateAndGet(World::pickBeeper)
        callbacks.update()
    }

    protected fun dropBeeper(callerPosition: Int) {
        callbacks.pauseAt(callerPosition)
        atomicWorld.updateAndGet(World::dropBeeper)
        callbacks.update()
    }

    protected fun onBeeper() = atomicWorld.get().onBeeper()

    protected fun beeperAhead() = atomicWorld.get().beeperAhead()

    protected fun leftIsClear() = atomicWorld.get().leftIsClear()

    protected fun frontIsClear() = atomicWorld.get().frontIsClear()

    protected fun rightIsClear() = atomicWorld.get().rightIsClear()
}
