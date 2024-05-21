package logic

import vm.Karel
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.atomic.AtomicReference

private val constructorType = MethodType.methodType(Void.TYPE, AtomicReference::class.java, Karel.Callbacks::class.java)
private val methodType = MethodType.methodType(Void.TYPE, Integer.TYPE)

fun Class<out Karel>.execute(atomicWorld: AtomicReference<World>, callbacks: Karel.Callbacks, main: String) {
    val lookup = MethodHandles.publicLookup()
    val constructor = lookup.findConstructor(this, constructorType)
    val instance = constructor.invoke(atomicWorld, callbacks)
    val method = lookup.findVirtual(this, main, methodType)
    method.invoke(instance, -1)
}
