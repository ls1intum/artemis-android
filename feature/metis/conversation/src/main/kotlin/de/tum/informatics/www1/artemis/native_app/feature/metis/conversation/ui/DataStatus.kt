package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

sealed class DataStatus : Comparable<DataStatus> {

    abstract val num: Int

    override fun compareTo(other: DataStatus): Int = num.compareTo(other.num)

    data object UpToDate : DataStatus() {
        override val num: Int = 2
    }

    data object Loading : DataStatus() {
        override val num: Int = 1
    }

    data object Outdated : DataStatus() {
        override val num: Int = 0
    }
}