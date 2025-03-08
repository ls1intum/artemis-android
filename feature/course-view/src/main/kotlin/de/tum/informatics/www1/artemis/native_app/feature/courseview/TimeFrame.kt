package de.tum.informatics.www1.artemis.native_app.feature.courseview

sealed class TimeFrame<T>(val items: List<T>) {

        abstract val key: String

        class Past<T>(items: List<T>) : TimeFrame<T>(items) {
            override val key: String = "past"
        }

        class Current<T>(items: List<T>) : TimeFrame<T>(items) {
            override val key: String = "current"
        }

        class Future<T>(items: List<T>) : TimeFrame<T>(items) {
            override val key: String = "future"
        }

        class NoDate<T>(items: List<T>) : TimeFrame<T>(items) {
            override val key: String = "noDate"
        }

        class DueSoon<T>(items: List<T>) : TimeFrame<T>(items) {
            override val key: String = "dueSoon"
        }

}