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

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
