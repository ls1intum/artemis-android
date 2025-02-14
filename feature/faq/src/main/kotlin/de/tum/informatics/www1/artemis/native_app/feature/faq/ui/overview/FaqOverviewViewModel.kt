package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory
import kotlinx.coroutines.flow.MutableSharedFlow
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
) : ViewModel() {

    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val allFaqs: StateFlow<DataState<List<Faq>>> = onRequestReload.onStart { emit(Unit) }.flatMapLatest {
        faqRepository.getFaqs(
            courseId = courseId
        )
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategories: MutableStateFlow<List<FaqCategory>> = MutableStateFlow(emptyList())
    val selectedCategories: StateFlow<List<FaqCategory>> = _selectedCategories

    val allCategories: StateFlow<List<FaqCategory>> = allFaqs.map {
        it.bind { faqs ->
            val all = faqs.flatMap { faq -> faq.categories }.toSet().toList()
            _selectedCategories.tryEmit(all)    // Select all by default
            all
        }.orElse(emptyList())
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    val displayedFaqs: StateFlow<DataState<List<Faq>>> = combine(
        allFaqs,
        searchQuery,
        selectedCategories,
    ) { faqsDataState, query, categories ->
        val filteredByCategories = faqsDataState.bind { faqs ->
            faqs.filter { faq ->
                faq.categories.any { categories.contains(it) }
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

    fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }

    fun updateQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onToggleSelectableFaqCategory(category: FaqCategory) {
        val isSelected = _selectedCategories.value.contains(category)
        if (isSelected) {
            _selectedCategories.value -= category
        } else {
            _selectedCategories.value += category
        }
    }
}