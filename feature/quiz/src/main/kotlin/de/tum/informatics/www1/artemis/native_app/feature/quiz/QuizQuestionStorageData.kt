package de.tum.informatics.www1.artemis.native_app.feature.quiz

interface DragAndDropStorageData {
    val value: Map<DropLocationId, DragItemId>
}

interface MultipleChoiceStorageData {
    val value: Map<AnswerOptionId, Boolean>
}

interface ShortAnswerStorageData {
    val value: Map<Int, String>
}

internal typealias DropLocationId = Long
internal typealias DragItemId = Long
internal typealias AnswerOptionId = Long

