package de.tum.informatics.www1.artemis.native_app.feature.metistest

import android.annotation.SuppressLint
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.asSnapshot


@SuppressLint("VisibleForTests")
suspend fun <T : Any> PagingSource<Int, T>.loadAsList(): List<T> {
    return Pager(PagingConfig(pageSize = 10), pagingSourceFactory = { this }).flow.asSnapshot {
        scrollTo(50)
    }
}