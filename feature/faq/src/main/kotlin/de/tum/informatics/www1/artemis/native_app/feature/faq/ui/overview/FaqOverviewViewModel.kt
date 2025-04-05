package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FaqOverviewViewModel(
    courseId: Long,
    private val faqRepository: FaqRepository,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {

    private val allFaqs: StateFlow<DataState<List<Faq>>> = requestReload.onStart { emit(Unit) }.flatMapLatest {
        faqRepository.getFaqs(
            courseId = courseId
        )
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory: MutableStateFlow<FaqCategory?> = MutableStateFlow(null)
    val selectedCategory: StateFlow<FaqCategory?> = _selectedCategory

    val allCategories: StateFlow<List<FaqCategory>> = allFaqs.map { dataState ->
        dataState.bind { faqs ->
            faqs.flatMap { faq -> faq.categories }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { it.key }
        }.orElse(emptyList())
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    val displayedFaqs: StateFlow<DataState<List<Faq>>> = combine(
        allFaqs,
        searchQuery,
        selectedCategory,
    ) { faqsDataState, query, selectedCategory ->
        val filteredByCategories = faqsDataState.bind { faqs ->
            if (selectedCategory == null) return@bind faqs

            faqs.filter { faq ->
                faq.categories.contains(selectedCategory)
            }
        }

        if (query.isBlank()) {
            return@combine filteredByCategories
        }

        filteredByCategories.bind { faqs ->
            faqs.filter {
                it.questionTitle.contains(query, ignoreCase = true) ||
                it.questionAnswer.contains(query, ignoreCase = true)
            }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    fun updateQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onToggleSelectableFaqCategory(category: FaqCategory) {
        val isSelected = _selectedCategory.value == category
        _selectedCategory.value = if (isSelected) null else category

    }
}