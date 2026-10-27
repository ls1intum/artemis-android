package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@Category(UnitTest::class)
class QuizTrainingSessionTest {
    private class Api : QuizTrainingApi {
        val requests = mutableListOf<TrainingPageRequest>()
        val submissions = mutableListOf<JsonObject>()
        var pages = ArrayDeque<TrainingPage>()
        var failPage = false
        var failSubmit = false
        var waitSubmit: CompletableDeferred<Unit>? = null

        var waitOverview: CompletableDeferred<Unit>? = null
        var hasSettings = false
        var overviewScore = 0.0
        var failOverview = false
        var failSettings = false
        var waitSettings: CompletableDeferred<Unit>? = null
        val privacyWrites = mutableListOf<Boolean>()

        override suspend fun loadOverview(): TrainingOverview {
            waitOverview?.await()
            if (failOverview) error("overview unavailable")
            return TrainingOverview(
                hasUserSetSettings = hasSettings,
                currentUserEntry = TrainingEntry(score = overviewScore),
            )
        }

        override suspend fun loadSettings(): TrainingPrivacy {
            waitSettings?.await()
            if (failSettings) error("settings unavailable")
            return TrainingPrivacy(false)
        }

        override suspend fun updatePrivacy(show: Boolean) {
            privacyWrites += show
        }

        override suspend fun loadPage(request: TrainingPageRequest): TrainingPage {
            requests += request
            if (failPage) error("offline")
            return pages.removeFirst()
        }

        override suspend fun submit(
            question: TrainingQuestion,
            payload: JsonObject,
        ): SubmittedAnswer {
            submissions += payload
            waitSubmit?.await()
            if (failSubmit) error("confirmation lost")
            return MultipleChoiceSubmittedAnswer(
                quizQuestion = question.quizQuestionWithSolutionDTO,
                scoreInPoints = null,
            )
        }
    }

    private fun question(id: Long, rated: Boolean = true, snapshot: List<Long>? = listOf(80, 90)) =
        TrainingQuestion(
            MultipleChoiceQuizQuestion(
                id = id,
                answerOptions =
                    listOf(MultipleChoiceQuizQuestion.AnswerOption(id = 100, isCorrect = true)),
            ),
            rated,
            snapshot,
            false,
        )

    private fun page(vararg qs: TrainingQuestion, more: Boolean = false) =
        TrainingPage(qs.toList(), more)

    @Test
    fun `rated snapshot remains unchanged after answers and page failure retries exact index`() =
        runBlocking {
            val api = Api()
            api.pages += page(question(1), more = true)
            api.pages += page(question(2))
            val s = QuizTrainingSession(api)
            s.start()
            s.submit()
            api.failPage = true
            s.next()
            assertEquals(TrainingStage.PAGE_ERROR, s.state.value.stage)
            api.failPage = false
            s.retryPage()
            assertEquals(
                listOf(
                    TrainingPageRequest(),
                    TrainingPageRequest(1, listOf(80, 90), false),
                    TrainingPageRequest(1, listOf(80, 90), false),
                ),
                api.requests,
            )
            assertEquals(2L, s.state.value.current?.quizQuestionWithSolutionDTO?.id)
        }

    @Test
    fun `legacy null snapshot continues true empty and refuses rated transition`() = runBlocking {
        val api = Api()
        api.pages += page(question(1, false, null), more = true)
        api.pages += page(question(2, true))
        val s = QuizTrainingSession(api)
        s.start()
        assertEquals(TrainingStage.UNRATED_CONFIRMATION, s.state.value.stage)
        s.acceptUnrated()
        s.submit()
        s.next()
        assertEquals(TrainingPageRequest(1, emptyList(), true), api.requests.last())
        assertEquals(TrainingStage.RESTART_REQUIRED, s.state.value.stage)
        assertNull(s.state.value.current)
    }

    @Test
    fun `nonnull unrated snapshot is not reset and unrated confirmation occurs once per transition`() =
        runBlocking {
            val api = Api()
            api.pages += page(question(1, false), more = true)
            api.pages += page(question(2, false))
            val s = QuizTrainingSession(api)
            s.start()
            s.acceptUnrated()
            s.submit()
            s.next()
            assertEquals(TrainingPageRequest(1, listOf(80, 90), false), api.requests.last())
            assertEquals(TrainingStage.ANSWERING, s.state.value.stage)
        }

    @Test
    fun `ten confirmed answers offer summary and continue reaches later page`() = runBlocking {
        val api = Api()
        api.pages += TrainingPage((1L..10L).map { question(it) }, true)
        api.pages += page(question(11))
        val s = QuizTrainingSession(api)
        s.start()
        repeat(10) {
            s.submit()
            s.next()
        }
        assertEquals(TrainingStage.SUMMARY, s.state.value.stage)
        assertEquals(10, s.state.value.answered)
        s.continueTraining()
        assertEquals(11L, s.state.value.current?.quizQuestionWithSolutionDTO?.id)
    }

    @Test
    fun `ambiguous submit never replays automatically and explicit retry freezes answer`() =
        runBlocking {
            val api = Api()
            api.pages += page(question(1))
            api.failSubmit = true
            val s = QuizTrainingSession(api)
            s.start()
            s.selectOption(100, true)
            s.submit()
            assertEquals(TrainingStage.UNCERTAIN, s.state.value.stage)
            s.selectOption(100, false)
            s.submit()
            s.retryPage()
            assertEquals(1, api.submissions.size)
            api.failSubmit = false
            s.retrySubmission()
            assertEquals(api.submissions[0], api.submissions[1])
            assertEquals(TrainingStage.EVALUATED, s.state.value.stage)
            s.submit()
            assertEquals(2, api.submissions.size)
        }

    @Test
    fun `inflight lock and invalidated session discard stale response and clear content`() =
        runBlocking {
            val api = Api()
            api.pages += page(question(1))
            api.waitSubmit = CompletableDeferred()
            val s = QuizTrainingSession(api)
            s.start()
            val job = launch { s.submit() }
            yield()
            s.submit()
            assertEquals(1, api.submissions.size)
            s.invalidate()
            api.waitSubmit!!.complete(Unit)
            job.join()
            assertEquals(TrainingStage.ACCESS_LOST, s.state.value.stage)
            assertNull(s.state.value.current)
            assertTrue(s.state.value.draft.options.isEmpty())
        }

    @Test
    fun `overview initialization retains original onboarding flag without writes`() = runBlocking {
        val s = QuizTrainingSession(Api())
        s.loadOverview()
        s.loadOverview()
        assertEquals(false, s.state.value.overview?.hasUserSetSettings)
        assertEquals(false, s.state.value.privacy?.showInLeaderboard)
    }

    @Test
    fun `empty page claiming continuation is recoverable and retries same page`() = runBlocking {
        val api = Api()
        api.pages += page(more = true)
        api.pages += page(question(1))
        val s = QuizTrainingSession(api)
        s.start()
        assertEquals(TrainingStage.PAGE_ERROR, s.state.value.stage)
        s.retryPage()
        assertEquals(listOf(TrainingPageRequest(), TrainingPageRequest()), api.requests)
    }

    @Test
    fun `answer projection hides solutions and short answer payload uses stable spot id`() {
        val q =
            ShortAnswerQuizQuestion(
                id = 7,
                explanation = "secret",
                text = "[-spot 1]",
                spots = listOf(ShortAnswerQuizQuestion.ShortAnswerSpot(id = 501, spotNr = 1)),
                solutions =
                    listOf(ShortAnswerQuizQuestion.ShortAnswerSolution(id = 3, text = "secret")),
                correctMappings = emptyList(),
            )
        val projected = trainingEditableQuestion(q) as ShortAnswerQuizQuestion
        assertNull(projected.explanation)
        assertTrue(projected.solutions.isEmpty())
        assertNull(projected.correctMappings)
        val payload = trainingPayload(q, TrainingDraft(texts = mapOf(1 to "yes")))
        assertEquals(
            501L,
            payload["submittedTexts"]!!
                .jsonArray
                .single()
                .jsonObject["spot"]!!
                .jsonObject["id"]!!
                .jsonPrimitive
                .long,
        )
        val mc =
            trainingEditableQuestion(question(1).quizQuestionWithSolutionDTO)
                as MultipleChoiceQuizQuestion
        assertNull(mc.answerOptions.single().isCorrect)
    }

    @Test
    fun `native evaluation adapter retains authoritative short answer correctness and missing points`() {
        val q =
            ShortAnswerQuizQuestion(
                id = 7,
                spots = listOf(ShortAnswerQuizQuestion.ShortAnswerSpot(id = 501, spotNr = 1)),
            )
        val evaluation =
            ShortAnswerSubmittedAnswer(
                quizQuestion = q,
                submittedTexts =
                    listOf(
                        ShortAnswerSubmittedAnswer.ShortAnswerSubmittedText(
                            text = "YES",
                            isCorrect = true,
                            spot = q.spots.single(),
                        )
                    ),
            )
        val result =
            trainingResultData(evaluation)
                as
                de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData.ShortAnswerData.Result
        assertEquals("YES", result.solutionTexts[1])
        assertEquals(true, result.correctness[1])
        assertNull(result.achievedPoints)
    }

    @Test
    fun `shared server evaluations decode all three native bodies with exact identities`() {
        val json =
            de.tum.informatics.www1.artemis.native_app.core.data.service.impl
                .JsonProvider()
                .applicationJsonConfiguration
        val text = javaClass.classLoader!!.getResource("training/evaluations.json")!!.readText()
        val evaluations = json.decodeFromString<List<SubmittedAnswer>>(text)
        assertEquals(3, evaluations.size)
        evaluations.forEach {
            assertEquals(it.quizQuestion!!.id, trainingResultData(it).question.id)
        }
        val sa = evaluations.filterIsInstance<ShortAnswerSubmittedAnswer>().single()
        val data =
            trainingResultData(sa)
                as
                de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizQuestionData.ShortAnswerData.Result
        assertEquals(913L, sa.submittedTexts.single { it.spot?.spotNr == 1 }.spot?.id)
        assertEquals(true, data.correctness[1])
        assertEquals(false, data.correctness[2])
        val dnd = evaluations.filterIsInstance<DragAndDropSubmittedAnswer>().single()
        assertEquals(4, (dnd.quizQuestion as DragAndDropQuizQuestion).correctMappings!!.size)
    }

    @Test
    fun `rated omitted empty snapshot continues false with an immutable empty array`() =
        runBlocking {
            val api = Api()
            api.pages += page(question(1, true, null), more = true)
            api.pages += page(question(2, true, null))
            val s = QuizTrainingSession(api)
            s.start()
            s.submit()
            s.next()
            assertEquals(TrainingPageRequest(1, emptyList(), false), api.requests.last())
            assertEquals(2L, s.state.value.current!!.quizQuestionWithSolutionDTO.id)
        }

    @Test
    fun `unsupported question can be skipped without submitting an answer`() = runBlocking {
        val api = Api()
        api.pages += page(question(1), question(2))
        val s = QuizTrainingSession(api)
        s.start()
        s.skipQuestion()
        assertEquals(2L, s.state.value.current!!.quizQuestionWithSolutionDTO.id)
        assertEquals(0, s.state.value.answered)
        assertTrue(api.submissions.isEmpty())
    }

    @Test
    fun `finishing after final evaluated question does not promise more questions`() = runBlocking {
        val api = Api()
        api.pages += page(question(1))
        val s = QuizTrainingSession(api)
        s.start()
        s.submit()
        s.finish()
        assertTrue(s.state.value.exhausted)
    }

    @Test
    fun `finishing and continuing an unanswered question preserves exact draft without requests`() =
        runBlocking {
            val api = Api()
            api.pages += page(question(1))
            val s = QuizTrainingSession(api)
            s.start()
            s.selectOption(100, true)
            val question = s.state.value.current
            val draft = s.state.value.draft
            s.finish()
            s.continueTraining()
            assertSame(question, s.state.value.current)
            assertEquals(draft, s.state.value.draft)
            assertEquals(1, api.requests.size)
            assertTrue(api.submissions.isEmpty())
        }

    @Test
    fun `starting training during overview read retains initial privacy onboarding`() =
        runBlocking {
            val api = Api()
            api.pages += page(question(1))
            api.waitOverview = CompletableDeferred()
            val s = QuizTrainingSession(api)
            val job = launch { s.loadOverview() }
            yield()
            s.start()
            api.waitOverview!!.complete(Unit)
            job.join()
            assertEquals(false, s.state.value.overview?.hasUserSetSettings)
        }

    @Test
    fun `first overview onboarding flag survives settings failure and subsequent overview read`() =
        runBlocking {
            val api = Api()
            api.failSettings = true
            val s = QuizTrainingSession(api)
            s.loadOverview()
            assertEquals(false, s.state.value.overview?.hasUserSetSettings)
            api.failSettings = false
            api.hasSettings = true
            s.loadOverview()
            assertEquals(false, s.state.value.overview?.hasUserSetSettings)
        }

    @Test
    fun `live rated wrappers omit an empty snapshot and retain rated continuation`() = runBlocking {
        val json =
            de.tum.informatics.www1.artemis.native_app.core.data.service.impl
                .JsonProvider()
                .applicationJsonConfiguration
        val text =
            javaClass.classLoader!!
                .getResource("training/live-rated-empty-snapshot.json")!!
                .readText()
        val questions = json.decodeFromString<List<TrainingQuestion>>(text)
        assertTrue(
            questions.all {
                it.isRated && it.questionIds == null && it.sessionQuestionIds == emptyList<Long>()
            }
        )
        val api = Api()
        api.pages += page(questions[0], more = true)
        api.pages += page(questions[1])
        val s = QuizTrainingSession(api)
        s.start()
        s.submit()
        s.next()
        assertEquals(TrainingPageRequest(1, emptyList(), false), api.requests.last())
        assertEquals(
            questions[1].quizQuestionWithSolutionDTO.id,
            s.state.value.current!!.quizQuestionWithSolutionDTO.id,
        )
    }

    @Test
    fun `a delayed settings read cannot replace an explicit visibility choice`() = runBlocking {
        val api = Api()
        val s = QuizTrainingSession(api)
        s.loadOverview()
        assertTrue(api.privacyWrites.isEmpty())
        api.waitSettings = CompletableDeferred()
        val job = launch { s.loadOverview() }
        yield()
        s.updatePrivacy(true)
        api.waitSettings!!.complete(Unit)
        job.join()
        assertEquals(listOf(true), api.privacyWrites)
        assertEquals(true, s.state.value.privacy?.showInLeaderboard)
    }

    @Test
    fun `mixed rating pages fail before accepting equal snapshots or any current question`() =
        runBlocking {
            for ((ratedSnapshot, unratedSnapshot) in
                listOf(listOf(80L, 90L) to listOf(80L, 90L), null to emptyList<Long>())) {
                val api = Api()
                api.pages +=
                    page(question(1, true, ratedSnapshot), question(2, false, unratedSnapshot))
                api.pages += page(question(3, true, listOf(7)))
                val s = QuizTrainingSession(api)
                s.start()
                assertEquals(TrainingStage.PAGE_ERROR, s.state.value.stage)
                assertNull(s.state.value.current)
                s.retryPage()
                assertEquals(TrainingPageRequest(), api.requests.last())
                assertEquals(3L, s.state.value.current!!.quizQuestionWithSolutionDTO.id)
                assertTrue(api.submissions.isEmpty())
            }
        }

    @Test
    fun `overview reentry refresh preserves confirmed work and original onboarding even after read failure`() =
        runBlocking {
            val api = Api()
            api.pages += page(question(1))
            val s = QuizTrainingSession(api)
            s.loadOverview()
            s.start()
            s.selectOption(100, true)
            s.submit()
            s.finish()
            val confirmed = s.state.value.evaluation
            val draft = s.state.value.draft
            api.hasSettings = true
            api.overviewScore = 2.0
            s.onOverviewResumed()
            assertEquals(2.0, s.state.value.overview!!.currentUserEntry!!.score)
            assertEquals(false, s.state.value.overview!!.hasUserSetSettings)
            assertSame(confirmed, s.state.value.evaluation)
            assertEquals(draft, s.state.value.draft)
            api.failOverview = true
            s.onOverviewResumed()
            assertTrue(s.state.value.overviewError)
            assertSame(confirmed, s.state.value.evaluation)
            assertEquals(1, api.submissions.size)
        }

    @Test
    fun `accessible setters retain exact drag member and blank IDs and ignore unavailable members`() =
        runBlocking {
            val api = Api()
            val dnd =
                DragAndDropQuizQuestion(
                    id = 41,
                    dragItems =
                        listOf(
                            DragAndDropQuizQuestion.DragItem(id = 501, text = "Alpha"),
                            DragAndDropQuizQuestion.DragItem(id = 502, text = "Beta"),
                        ),
                    dropLocations =
                        listOf(
                            DragAndDropQuizQuestion.DropLocation(id = 91),
                            DragAndDropQuizQuestion.DropLocation(id = 92),
                        ),
                )
            val sa =
                ShortAnswerQuizQuestion(
                    id = 42,
                    spots = listOf(ShortAnswerQuizQuestion.ShortAnswerSpot(id = 913, spotNr = 1)),
                )
            api.pages +=
                TrainingPage(
                    listOf(
                        TrainingQuestion(dnd, true, emptyList()),
                        TrainingQuestion(sa, true, emptyList()),
                    ),
                    false,
                )
            val s = QuizTrainingSession(api)
            s.start()
            s.moveItem(501, 91)
            s.moveItem(501, 92)
            s.moveItem(999, 91)
            s.moveItem(502, 999)
            assertEquals(mapOf(92L to 501L), s.state.value.draft.mappings)
            val mapping =
                trainingPayload(dnd, s.state.value.draft)["mappings"]!!
                    .jsonArray
                    .single()
                    .jsonObject
            assertEquals(92L, mapping["dropLocation"]!!.jsonObject["id"]!!.jsonPrimitive.long)
            assertEquals(501L, mapping["dragItem"]!!.jsonObject["id"]!!.jsonPrimitive.long)
            s.clearDrop(92)
            assertTrue(s.state.value.draft.mappings.isEmpty())
            s.skipQuestion()
            s.setText(913, "wrong member")
            s.setText(1, "binary search")
            assertEquals(mapOf(1 to "binary search"), s.state.value.draft.texts)
            assertEquals(
                913L,
                trainingPayload(sa, s.state.value.draft)["submittedTexts"]!!
                    .jsonArray
                    .single()
                    .jsonObject["spot"]!!
                    .jsonObject["id"]!!
                    .jsonPrimitive
                    .long,
            )
        }

    @Test
    fun `snapshot ID order does not change the training session`() = runBlocking {
        val api = Api()
        api.pages += page(
            question(1, snapshot = listOf(80, 90)),
            question(2, snapshot = listOf(90, 80)),
            more = true,
        )
        api.pages += page(question(3, snapshot = listOf(90, 80)))
        val s = QuizTrainingSession(api)
        s.start()
        repeat(2) {
            s.submit()
            s.next()
        }
        assertEquals(TrainingStage.ANSWERING, s.state.value.stage)
        assertEquals(3L, s.state.value.current!!.quizQuestionWithSolutionDTO.id)
        assertEquals(TrainingPageRequest(1, listOf(80, 90), false), api.requests.last())
    }
}
