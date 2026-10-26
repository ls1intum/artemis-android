package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.viewresult

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.FileUploadSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ModelingSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class ResultReviewSafetyTest {
    @Test fun `file and modeling submissions retain immutable submitted work`() {
        val json = JsonProvider().applicationJsonConfiguration
        val file = json.decodeFromString<Submission>("""{"submissionExerciseType":"file-upload","id":11,"filePath":"/api/core/files/file-upload-exercises/1/submissions/11/answer.pdf"}""") as FileUploadSubmission
        assertEquals("/api/core/files/file-upload-exercises/1/submissions/11/answer.pdf", file.filePath)
        val model = json.decodeFromString<Submission>("""{"submissionExerciseType":"modeling","id":12,"model":"{\"elements\":{}}","explanationText":"Assessed explanation"}""") as ModelingSubmission
        assertEquals("{\"elements\":{}}", model.model)
        assertEquals("Assessed explanation", model.explanationText)
    }

    @Test fun `submitted file URLs never send authentication to another origin`() {
        assertNull(reviewFileUrl("https://artemis.example", "https://evil.example/answer.pdf"))
        assertNull(reviewFileUrl("https://artemis.example", "//evil.example/answer.pdf"))
        assertNull(reviewFileUrl("https://artemis.example", "javascript:alert(1)"))
        assertNull(reviewFileUrl("https://artemis.example", "/api/core/files/../../private"))
        assertEquals("https://artemis.example/api/core/files/file-upload-exercises/1/answer.pdf", reviewFileUrl("https://artemis.example", "/api/core/files/file-upload-exercises/1/answer.pdf"))
    }

    @Test fun `shared student fixtures preserve public messages UTF16 and course rounding`() {
        val json = Json { ignoreUnknownKeys = true }
        val fixture = Json.parseToJsonElement(javaClass.getResourceAsStream("/native-result-fixtures.json")!!.reader().readText()).jsonObject
        fixture.getValue("feedbackCases").jsonArray.forEach { entry ->
            val item = entry.jsonObject
            val feedback = json.decodeFromJsonElement<ReviewFeedback>(item.getValue("input"))
            item["expectedPublicText"]?.jsonArray?.forEach { assertTrue(feedback.message.contains(it.jsonPrimitive.content)) }
            item["forbiddenText"]?.jsonArray?.forEach { assertFalse(json.encodeToString(ReviewFeedback.serializer(), feedback).contains(it.jsonPrimitive.content)) }
            item["expectedId"]?.let { assertEquals(it.jsonPrimitive.long, feedback.id) }
        }
        fixture.getValue("textCases").jsonArray.forEach { entry ->
            val item = entry.jsonObject
            val source = json.decodeFromJsonElement<ReviewSubmission>(JsonObject(item.filterKeys { it == "text" || it == "blocks" }))
            val feedback = json.decodeFromJsonElement<ReviewFeedback>(item.getValue("feedback"))
            val range = item["expectedUTF16Range"]
            if (range == JsonNull) assertNull(source.rangeFor(feedback)) else {
                assertEquals(range!!.jsonArray[0].jsonPrimitive.int until range.jsonArray[1].jsonPrimitive.int, source.rangeFor(feedback))
            }
        }
        assertEquals("112.5% · 11.3 / 10", reviewScore(112.5f, 10f, 1))
        assertEquals("81.23% · 12.19 / 15", reviewScore(81.234f, 15f, 2))
    }
}
