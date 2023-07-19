package de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

fun createCourseWithSelfRegistration(
    title: String,
    shortName: String,
    startDate: Instant = Clock.System.now(),
    endDate: Instant = startDate + 3.days
): String = """
{
  "id": null,
  "title": "$title",
  "shortName": "$shortName",
  "customizeGroupNames": true,
  "studentGroupName": "artemis-$shortName-student",
  "teachingAssistantGroupName": "artemis-$shortName-tutors",
  "editorGroupName": "artemis-$shortName-editors",
  "instructorGroupName": "artemis-$shortName-instructors",
  "startDate": "$startDate",
  "endDate": "$endDate",
  "semester": null,
  "testCourse": null,
  "onlineCourse": false,
  "complaintsEnabled": false,
  "requestMoreFeedbackEnabled": false,
  "maxPoints": null,
  "accuracyOfScores": 1,
  "defaultProgrammingLanguage": null,
  "maxComplaints": 0,
  "maxTeamComplaints": 0,
  "maxComplaintTimeDays": 0,
  "maxComplaintTextLimit": 0,
  "maxComplaintResponseTextLimit": 0,
  "maxRequestMoreFeedbackTimeDays": 0,
  "enrollmentStartDate": "$startDate",
  "enrollmentEndDate": "$endDate",
  "unenrollmentEnabled": null,
  "color": null,
  "courseIcon": null,
  "timeZone": null,
  "courseInformationSharingConfiguration": "DISABLED",
  "enrollmentEnabled": true,
  "enrollmentConfirmationMessage": "course enrollment message"
}
""".trimIndent()

fun createTextExercise(title: String, courseId: Long): String = """
    {
        "mode": "INDIVIDUAL",
        "includedInOverallScore": "INCLUDED_COMPLETELY",
        "numberOfAssessmentsOfCorrectionRounds": [
            {
                "inTime": 0,
                "late": 0
            }
        ],
        "studentAssignedTeamIdComputed": false,
        "secondCorrectionEnabled": false,
        "type": "text",
        "bonusPoints": 0,
        "isAtLeastTutor": false,
        "isAtLeastEditor": false,
        "isAtLeastInstructor": false,
        "teamMode": false,
        "assessmentDueDateError": false,
        "dueDateError": false,
        "exampleSolutionPublicationDateError": false,
        "exampleSolutionPublicationDateWarning": false,
        "presentationScoreEnabled": false,
        "assessmentType": "MANUAL",
        "title": "$title",
        "maxPoints": 10,
        "problemStatement": "Android Problem Statement",
        "exampleSolution": "Android Example Solution",
        "gradingInstructions": "Android Assessment Instructions",
        "channelName": "${(title + "_c").take(30)}",
        "course": { "id": $courseId }
    }
""".trimIndent()

fun createModelingExercise(title: String, courseId: Long): String = """
    {
        "title": "$title",        
        "mode": "INDIVIDUAL",
        "includedInOverallScore": "INCLUDED_COMPLETELY",
        "numberOfAssessmentsOfCorrectionRounds": [
            {
                "inTime": 0,
                "late": 0
            }
        ],
        "studentAssignedTeamIdComputed": false,
        "secondCorrectionEnabled": false,
        "type": "modeling",
        "bonusPoints": 10,
        "isAtLeastTutor": false,
        "isAtLeastEditor": false,
        "isAtLeastInstructor": false,
        "teamMode": false,
        "assessmentDueDateError": false,
        "dueDateError": false,
        "exampleSolutionPublicationDateError": false,
        "exampleSolutionPublicationDateWarning": false,
        "presentationScoreEnabled": false,
        "diagramType": "ClassDiagram",
        "assessmentType": "MANUAL",
        "title": "Cypress Modeling Exercise",
        "categories": ["{\"category\":\"android-e2e\",\"color\":\"#6ae8ac\"}"],
        "difficulty": "EASY",
        "maxPoints": 10,
        "problemStatement": "Problem Statement",
        "gradingInstructions": "Grading Instruction ",
        "exampleSolutionExplanation": "Example Solution Explanation",
        "course": { "id": $courseId },
        "channelName": "${(title + "_c").take(30)}",
        "exampleSolutionModel": "{\"version\":\"2.0.0\",\"type\":\"ClassDiagram\",\"size\":{\"width\":640,\"height\":600},\"interactive\":{\"elements\":[],\"relationships\":[]},\"elements\":[{\"id\":\"6e1f57c6-cbc7-4b97-9df9-c5741dc905fa\",\"name\":\"Package\",\"type\":\"Package\",\"owner\":null,\"bounds\":{\"x\":230,\"y\":0,\"width\":200,\"height\":100}},{\"id\":\"ff7e3be0-9765-4301-baf5-cf9cf2f17c3c\",\"name\":\"Class\",\"type\":\"Class\",\"owner\":null,\"bounds\":{\"x\":0,\"y\":220,\"width\":200,\"height\":100},\"attributes\":[\"de2d464b-f969-4cf3-ac0d-2f300b3a6497\"],\"methods\":[\"084a59b9-3009-4ebd-885c-159b436581d9\"]},{\"id\":\"de2d464b-f969-4cf3-ac0d-2f300b3a6497\",\"name\":\"+ attribute: Type\",\"type\":\"ClassAttribute\",\"owner\":\"ff7e3be0-9765-4301-baf5-cf9cf2f17c3c\",\"bounds\":{\"x\":0,\"y\":260,\"width\":200,\"height\":30}},{\"id\":\"084a59b9-3009-4ebd-885c-159b436581d9\",\"name\":\"+ method()\",\"type\":\"ClassMethod\",\"owner\":\"ff7e3be0-9765-4301-baf5-cf9cf2f17c3c\",\"bounds\":{\"x\":0,\"y\":290,\"width\":200,\"height\":30}},{\"id\":\"1ad94aff-ee37-494f-8b99-9d861dc58e4a\",\"name\":\"Abstract\",\"type\":\"AbstractClass\",\"owner\":null,\"bounds\":{\"x\":380,\"y\":220,\"width\":200,\"height\":110},\"attributes\":[\"4d58287c-d4c0-42d1-9d02-ad239b701de6\"],\"methods\":[\"2de2a7c3-19c9-4534-8a71-f6607e93556c\"]},{\"id\":\"4d58287c-d4c0-42d1-9d02-ad239b701de6\",\"name\":\"+ attribute: Type\",\"type\":\"ClassAttribute\",\"owner\":\"1ad94aff-ee37-494f-8b99-9d861dc58e4a\",\"bounds\":{\"x\":380,\"y\":270,\"width\":200,\"height\":30}},{\"id\":\"2de2a7c3-19c9-4534-8a71-f6607e93556c\",\"name\":\"+ method()\",\"type\":\"ClassMethod\",\"owner\":\"1ad94aff-ee37-494f-8b99-9d861dc58e4a\",\"bounds\":{\"x\":380,\"y\":300,\"width\":200,\"height\":30}}],\"relationships\":[],\"assessments\":[]}"
    }

""".trimIndent()

fun createProgramingExercise(title: String, courseId: Long): String = """
    {
        "mode": "INDIVIDUAL",
        "includedInOverallScore": "INCLUDED_COMPLETELY",
        "numberOfAssessmentsOfCorrectionRounds": [
            {
                "inTime": 0,
                "late": 0
            }
        ],
        "studentAssignedTeamIdComputed": false,
        "secondCorrectionEnabled": false,
        "type": "programming",
        "bonusPoints": 0,
        "isAtLeastTutor": false,
        "isAtLeastEditor": false,
        "isAtLeastInstructor": false,
        "teamMode": false,
        "assessmentDueDateError": false,
        "dueDateError": false,
        "exampleSolutionPublicationDateError": false,
        "exampleSolutionPublicationDateWarning": false,
        "presentationScoreEnabled": false,
        "templateParticipation": {
            "type": "template"
        },
        "solutionParticipation": {
            "type": "solution"
        },
        "publishBuildPlanUrl": false,
        "allowOnlineEditor": true,
        "staticCodeAnalysisEnabled": false,
        "allowOfflineIde": true,
        "programmingLanguage": "JAVA",
        "noVersionControlAndContinuousIntegrationAvailable": false,
        "checkoutSolutionRepository": false,
        "projectType": "PLAIN_MAVEN",
        "showTestNamesToStudents": false,
        "assessmentType": "AUTOMATIC",
        "problemStatement": "# Sorting with the Strategy Pattern\n\nIn this exercise, we want to implement sorting algorithms and choose them based on runtime specific variables.\n\n### Part 1: Sorting\n\nFirst, we need to implement two sorting algorithms, in this case `MergeSort` and `BubbleSort`.\n\n**You have the following tasks:**\n\n1. [task][Implement Bubble Sort](testBubbleSort)\nImplement the method `performSort(List<Date>)` in the class `BubbleSort`. Make sure to follow the Bubble Sort algorithm exactly.\n\n2. [task][Implement Merge Sort](testMergeSort)\nImplement the method `performSort(List<Date>)` in the class `MergeSort`. Make sure to follow the Merge Sort algorithm exactly.\n\n### Part 2: Strategy Pattern\n\nWe want the application to apply different algorithms for sorting a `List` of `Date` objects.\nUse the strategy pattern to select the right sorting algorithm at runtime.\n\n**You have the following tasks:**\n\n1. [task][SortStrategy Interface](testClass[SortStrategy],testMethods[SortStrategy])\nCreate a `SortStrategy` interface and adjust the sorting algorithms so that they implement this interface.\n\n2. [task][Context Class](testAttributes[Context],testMethods[Context])\nCreate and implement a `Context` class following the below class diagram\n\n3. [task][Context Policy](testConstructors[Policy],testAttributes[Policy],testMethods[Policy])\nCreate and implement a `Policy` class following the below class diagram with a simple configuration mechanism:\n\n    1. [task][Select MergeSort](testClass[MergeSort],testUseMergeSortForBigList)\n    Select `MergeSort` when the List has more than 10 dates.\n\n    2. [task][Select BubbleSort](testClass[BubbleSort],testUseBubbleSortForSmallList)\n    Select `BubbleSort` when the List has less or equal 10 dates.\n\n4. Complete the `Client` class which demonstrates switching between two strategies at runtime.\n\n@startuml\n\nclass Client {\n}\n\nclass Policy {\n  <color:testsColor(testMethods[Policy])>+configure()</color>\n}\n\nclass Context {\n  <color:testsColor(testAttributes[Context])>-dates: List<Date></color>\n  <color:testsColor(testMethods[Context])>+sort()</color>\n}\n\ninterface SortStrategy {\n  <color:testsColor(testMethods[SortStrategy])>+performSort(List<Date>)</color>\n}\n\nclass BubbleSort {\n  <color:testsColor(testBubbleSort)>+performSort(List<Date>)</color>\n}\n\nclass MergeSort {\n  <color:testsColor(testMergeSort)>+performSort(List<Date>)</color>\n}\n\nMergeSort -up-|> SortStrategy #testsColor(testClass[MergeSort])\nBubbleSort -up-|> SortStrategy #testsColor(testClass[BubbleSort])\nPolicy -right-> Context #testsColor(testAttributes[Policy]): context\nContext -right-> SortStrategy #testsColor(testAttributes[Context]): sortAlgorithm\nClient .down.> Policy\nClient .down.> Context\n\nhide empty fields\nhide empty methods\n\n@enduml\n\n\n### Part 3: Optional Challenges\n\n(These are not tested)\n\n1. Create a new class `QuickSort` that implements `SortStrategy` and implement the Quick Sort algorithm.\n\n2. Make the method `performSort(List<Dates>)` generic, so that other objects can also be sorted by the same method.\n**Hint:** Have a look at Java Generics and the interface `Comparable`.\n\n3. Think about a useful decision in `Policy` when to use the new `QuickSort` algorithm.\n",
        "title": "$title",
        "shortName": "<Insert programming exercise short name here>",
        "maxPoints": 10,
        "packageName": "<Insert package name here>",
        "channelName": "${(title + "_c").take(30)}",
        "course": { "id": $courseId }
    }

""".trimIndent()

fun createQuizExercise(
    title: String,
    courseId: Long,
    backgroundFilePath: String,
    mode: QuizExercise.QuizMode = QuizExercise.QuizMode.INDIVIDUAL
): String = """
{
  "title": "$title",
  "bonusPoints": 0,
  "allowComplaintsForAutomaticAssessments": false,
  "allowManualFeedbackRequests": false,
  "mode": "INDIVIDUAL",
  "includedInOverallScore": "NOT_INCLUDED",
  "type": "quiz",
  "course": {
    "id": $courseId
  },
  "numberOfAssessmentsOfCorrectionRounds": [
    {
      "inTime": 0,
      "late": 0
    }
  ],
  "studentAssignedTeamIdComputed": false,
  "secondCorrectionEnabled": false,
  "isAtLeastTutor": false,
  "isAtLeastEditor": false,
  "isAtLeastInstructor": false,
  "teamMode": false,
  "assessmentDueDateError": false,
  "exampleSolutionPublicationDateError": false,
  "exampleSolutionPublicationDateWarning": false,
  "presentationScoreEnabled": false,
  "allowedNumberOfAttempts": 1,
  "randomizeQuestionOrder": true,
  "isOpenForPractice": false,
  "duration": 600,
  "quizQuestions": [
    {
      "title": "MC1",
      "text": "Enter your long question if needed",
      "hint": "Add a hint here (visible during the quiz via ?-Button)",
      "points": 1,
      "scoringType": "ALL_OR_NOTHING",
      "randomizeOrder": true,
      "invalid": false,
      "exportQuiz": false,
      "type": "multiple-choice",
      "answerOptions": [
        {
          "text": "Enter a correct answer option here",
          "hint": "Add a hint here (visible during the quiz via ?-Button)",
          "explanation": "Add an explanation here (only visible in feedback after quiz has ended)",
          "isCorrect": true,
          "invalid": false
        },
        {
          "text": "Enter a wrong answer option here",
          "isCorrect": false,
          "invalid": false
        }
      ]
    },
    {
      "title": "ShortAnswer",
      "text": "Enter your long question if needed\n\nSelect a part of the text and click on Add Spot to automatically create an input field and the corresponding mapping\n\nYou can define a input field like this: This [-spot 1] an [-spot 2] field.\n\nTo define the solution for the input fields you need to create a mapping (multiple mapping also possible):",
      "points": 1,
      "scoringType": "PROPORTIONAL_WITHOUT_PENALTY",
      "randomizeOrder": true,
      "invalid": false,
      "exportQuiz": false,
      "type": "short-answer",
      "spots": [
        {
          "tempID": 8712716567622115,
          "width": 15,
          "spotNr": 1,
          "invalid": false
        },
        {
          "tempID": 1322472840644149,
          "width": 15,
          "spotNr": 2,
          "invalid": false
        }
      ],
      "solutions": [
        {
          "tempID": 2161480876583171,
          "text": "is",
          "invalid": false
        },
        {
          "tempID": 2629748690868899,
          "text": "input",
          "invalid": false
        },
        {
          "tempID": 3255797993079859,
          "text": "correctInBothFields",
          "invalid": false
        }
      ],
      "correctMappings": [
        {
          "invalid": false,
          "solution": {
            "tempID": 2161480876583171,
            "text": "is",
            "invalid": false
          },
          "spot": {
            "tempID": 8712716567622115,
            "width": 15,
            "spotNr": 1,
            "invalid": false
          }
        },
        {
          "invalid": false,
          "solution": {
            "tempID": 2629748690868899,
            "text": "input",
            "invalid": false
          },
          "spot": {
            "tempID": 1322472840644149,
            "width": 15,
            "spotNr": 2,
            "invalid": false
          }
        },
        {
          "invalid": false,
          "solution": {
            "tempID": 3255797993079859,
            "text": "correctInBothFields",
            "invalid": false
          },
          "spot": {
            "tempID": 8712716567622115,
            "width": 15,
            "spotNr": 1,
            "invalid": false
          }
        },
        {
          "invalid": false,
          "solution": {
            "tempID": 3255797993079859,
            "text": "correctInBothFields",
            "invalid": false
          },
          "spot": {
            "tempID": 1322472840644149,
            "width": 15,
            "spotNr": 2,
            "invalid": false
          }
        }
      ],
      "matchLetterCase": false,
      "similarityValue": 85
    },
    {
      "title": "Dnd Question",
      "text": "Enter your long question if needed",
      "hint": "Add a hint here (visible during the quiz via ?-Button)",
      "points": 1,
      "scoringType": "PROPORTIONAL_WITH_PENALTY",
      "randomizeOrder": true,
      "invalid": false,
      "exportQuiz": false,
      "type": "drag-and-drop",
      "backgroundFilePath": "$backgroundFilePath",
      "dropLocations": [
        {
          "tempID": 7167046265873659,
          "posX": 22,
          "posY": 57,
          "width": 23,
          "height": 23,
          "invalid": false
        },
        {
          "tempID": 7845820172921351,
          "posX": 94,
          "posY": 55,
          "width": 26,
          "height": 20,
          "invalid": false
        }
      ],
      "dragItems": [
        {
          "tempID": 1600501544974459,
          "text": "item1",
          "invalid": false
        },
        {
          "tempID": 3318321452932735,
          "text": "item2",
          "invalid": false
        }
      ],
      "correctMappings": [
        {
          "invalid": false,
          "dragItem": {
            "tempID": 1600501544974459,
            "text": "item1",
            "invalid": false
          },
          "dropLocation": {
            "tempID": 7167046265873659,
            "posX": 22,
            "posY": 57,
            "width": 23,
            "height": 23,
            "invalid": false
          }
        },
        {
          "invalid": false,
          "dragItem": {
            "tempID": 3318321452932735,
            "text": "item2",
            "invalid": false
          },
          "dropLocation": {
            "tempID": 7845820172921351,
            "posX": 94,
            "posY": 55,
            "width": 26,
            "height": 20,
            "invalid": false
          }
        }
      ]
    }
  ],
  "quizMode": "${mode.name}",
  "isActiveQuiz": false,
  "isPracticeModeAvailable": true,
  "isEditable": true,
  "channelName": "${(title + "_c").take(30)}"
}
""".trimIndent()

fun createTextLectureUnit(name: String) = """
    {"name":"$name","releaseDate":null,"competencies":[],"type":"text","content":"${generateId()}"}
""".trimIndent()

fun createExerciseLectureUnit(
    @Suppress("UNUSED_PARAMETER") name: String,
    exerciseAsString: String
) = """
    {
      "type": "exercise",
      "exercise": $exerciseAsString
    }
""".trimIndent()

fun createVideoLectureUnit(name: String) = """
    {
      "name": "$name",
      "competencies": [],
      "type": "video",
      "description": "${generateId()}",
      "source": "https://example.com/"
    }
""".trimIndent()

fun createOnlineLectureUnit(name: String) = """
    {
      "name": "$name",
      "competencies": [],
      "type": "online",
      "description": "${generateId()}",
      "source": "https://example.com/"
    }
""".trimIndent()
