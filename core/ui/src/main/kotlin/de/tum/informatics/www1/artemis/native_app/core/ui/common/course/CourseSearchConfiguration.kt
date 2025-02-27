package de.tum.informatics.www1.artemis.native_app.core.ui.common.course

sealed class CourseSearchConfiguration {
    data class Search(
        val query: String,
        val hint: String,
        val onUpdateQuery: (String) -> Unit
    ) : CourseSearchConfiguration()

    data object DisabledSearch: CourseSearchConfiguration()
}