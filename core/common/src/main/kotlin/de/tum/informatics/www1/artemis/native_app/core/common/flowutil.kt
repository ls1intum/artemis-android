package de.tum.informatics.www1.artemis.native_app.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine


@Suppress("UNCHECKED_CAST")
fun <A, B, C, D, E, F, G> combine6(
    f1: Flow<A>,
    f2: Flow<B>,
    f3: Flow<C>,
    f4: Flow<D>,
    f5: Flow<E>,
    f6: Flow<F>,
    transform: (A, B, C, D, E, F) -> G
): Flow<G> {
    return combine(f1, f2, f3, f4, f5, f6) {
        val v1 = it[0] as A
        val v2 = it[1] as B
        val v3 = it[2] as C
        val v4 = it[3] as D
        val v5 = it[4] as E
        val v6 = it[5] as F

        transform(v1, v2, v3, v4, v5, v6)
    }
}

@Suppress("UNCHECKED_CAST")
fun <A, B, C, D, E, F, G, H> combine7(
    f1: Flow<A>,
    f2: Flow<B>,
    f3: Flow<C>,
    f4: Flow<D>,
    f5: Flow<E>,
    f6: Flow<F>,
    f7: Flow<G>,
    transform: (A, B, C, D, E, F, G) -> H
): Flow<H> {
    return combine(f1, f2, f3, f4, f5, f6, f7) {
        val v1 = it[0] as A
        val v2 = it[1] as B
        val v3 = it[2] as C
        val v4 = it[3] as D
        val v5 = it[4] as E
        val v6 = it[5] as F
        val v7 = it[6] as G

        transform(v1, v2, v3, v4, v5, v6, v7)
    }
}