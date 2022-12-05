package de.tum.informatics.www1.artemis.native_app.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector


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