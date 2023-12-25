package common

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.BiConsumer

fun <T> CompletableFuture<T>.whenCompleteAsync(executor: Executor, action: BiConsumer<T?, Throwable?>):
        CompletableFuture<T> = whenCompleteAsync(action, executor)
