package de.tum.informatics.www1.artemis.native_app.core.data.service

object ApiEndpoint {

    private const val api = "api"

    val communication = arrayOf(api, "communication")

    val core = arrayOf(api, "core")
    val core_public = arrayOf(*core, "public")

    val exercise = arrayOf(api, "exercise")
    val exercise_exercises = arrayOf(*exercise, "exercises")

    val lecture = arrayOf(api, "lecture")
    val lecture_lectures = arrayOf(*lecture, "lectures")

    val text = arrayOf(api, "text")
    val text_textExercises = arrayOf(*text, "text-exercises")

    val modeling = arrayOf(api, "modeling")
    val modeling_modelingExercises = arrayOf(*modeling, "modeling-exercises")

    val programming = arrayOf(api, "programming")
    val programming_programmingExercises = arrayOf(*programming, "programming-exercises")

    val quiz = arrayOf(api, "quiz")
    val quiz_quizExercises = arrayOf(*quiz, "quiz-exercises")

}