package de.tum.informatics.www1.artemis.native_app.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.transformLatest

fun <T> Flow<T>.withPrevious(): Flow<Pair<T?, T>> = FlowWithPreviousElement(this)

class FlowWithPreviousElement<T>(private val wrapper: Flow<T>) : Flow<Pair<T?, T>> {

    private var previous: T? = null

    override suspend fun collect(collector: FlowCollector<Pair<T?, T>>) {
        wrapper.collect {
            collector.emit(previous to it)
            previous = it
        }
    }
}

fun <A, B, C> transformLatest(
    flow1: Flow<A>,
    flow2: Flow<B>,
    transform: suspend FlowCollector<C>.(A, B) -> Unit
): Flow<C> {
    return combine(flow1, flow2) { a, b -> a to b }
        .transformLatest { (a, b) -> transform(a, b) }
}

fun <A, B, C, D> transformLatest(
    flow1: Flow<A>,
    flow2: Flow<B>,
    flow3: Flow<C>,
    transform: suspend FlowCollector<D>.(A, B, C) -> Unit
): Flow<D> {
    return combine(flow1, flow2, flow3) { a, b, c -> Triple(a, b, c) }
        .transformLatest { (a, b, c) -> transform(a, b, c) }
}

fun <A, B, C, D, E> transformLatest(
    flow1: Flow<A>,
    flow2: Flow<B>,
    flow3: Flow<C>,
    flow4: Flow<D>,
    transform: suspend FlowCollector<E>.(A, B, C, D) -> Unit
): Flow<E> {
    return combine(flow1, flow2, flow3, flow4) { a, b, c, d -> Quadruple(a, b, c, d) }
        .transformLatest { (a, b, c, d) -> transform(a, b, c, d) }
}

fun <A, B, C> flatMapLatest(
    flow1: Flow<A>,
    flow2: Flow<B>,
    transform: suspend (A, B) -> Flow<C>
): Flow<C> {
    return combine(flow1, flow2) { a, b -> a to b }
        .flatMapLatest { (a, b) -> transform(a, b) }
}

fun <A, B, C, D> flatMapLatest(
    flow1: Flow<A>,
    flow2: Flow<B>,
    flow3: Flow<C>,
    transform: suspend (A, B, C) -> Flow<D>
): Flow<D> {
    return combine(flow1, flow2, flow3) { a, b, c -> Triple(a, b, c) }
        .flatMapLatest { (a, b, c) -> transform(a, b, c) }
}

fun <A, B, C, D, E> flatMapLatest(
    flow1: Flow<A>,
    flow2: Flow<B>,
    flow3: Flow<C>,
    flow4: Flow<D>,
    transform: suspend (A, B, C, D) -> Flow<E>
): Flow<E> {
    return combine(flow1, flow2, flow3, flow4, ::Quadruple)
        .flatMapLatest { (a, b, c, d) -> transform(a, b, c, d) }
}

fun <A, B, C, D, E, F> flatMapLatest(
    flow1: Flow<A>,
    flow2: Flow<B>,
    flow3: Flow<C>,
    flow4: Flow<D>,
    flow5: Flow<E>,
    transform: suspend (A, B, C, D, E) -> Flow<F>
): Flow<F> {
    return combine(flow1, flow2, flow3, flow4, flow5, ::Quintuple)
        .flatMapLatest { (a, b, c, d, e) -> transform(a, b, c, d, e) }
}

fun <A, B, C, D, E, F, G> flatMapLatest(
    flow1: Flow<A>,
    flow2: Flow<B>,
    flow3: Flow<C>,
    flow4: Flow<D>,
    flow5: Flow<E>,
    flow6: Flow<F>,
    transform: suspend (A, B, C, D, E, F) -> Flow<G>
): Flow<G> {
    return combine(flow1, flow2, flow3, flow4, flow5, flow6, ::Tuple6)
        .flatMapLatest { (a, b, c, d, e, f) -> transform(a, b, c, d, e, f) }
}

/**
 * Implementation of [combine] that supports six flows.
 * The default implementation of [combine] does not support more than five flows.
 *
 * This implementation is taken from [here](https://stackoverflow.com/a/73130632/13366254).
 *
 */
inline fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> {
    return combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
        )
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

private data class Quintuple<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

private data class Tuple6<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)
