package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import java.text.DecimalFormat

val ExercisePointsDecimalFormat: DecimalFormat = DecimalFormat("#.##")
val CoursePointsDecimalFormat: DecimalFormat = ExercisePointsDecimalFormat
/** Public score text shared by the exercise overview and assessed-work review. */
data class ExerciseResultScoreText(val percentage: String, val points: String?, val maximum: String?)

fun formatExerciseResultScore(
    score: Float?, maxPoints: Float?, precision: Int, locale: java.util.Locale = java.util.Locale.getDefault(),
): ExerciseResultScoreText? {
    if (score == null || !score.isFinite()) return null
    val percent = java.math.BigDecimal(score.toString())
    val maximum = maxPoints?.takeIf { it.isFinite() }?.let { java.math.BigDecimal(it.toString()) }
    val points = maximum?.takeIf { it.signum() > 0 }?.multiply(percent)?.divide(java.math.BigDecimal(100))
    return ExerciseResultScoreText(
        percentage = formatResultNumber(percent, precision, locale) + "%",
        points = points?.let { formatResultNumber(it, precision, locale) },
        maximum = maximum?.let { formatResultNumber(it, precision, locale) },
    )
}

fun formatExercisePoints(points: Float, precision: Int, locale: java.util.Locale = java.util.Locale.getDefault()): String =
    formatResultNumber(java.math.BigDecimal(points.toString()), precision, locale)

private fun formatResultNumber(value: java.math.BigDecimal, precision: Int, locale: java.util.Locale): String {
    val digits = precision.coerceIn(0, 10)
    // Match the web's decimal rounding, including ties towards +infinity for negative values.
    val rounded = value.setScale(digits, if (value.signum() < 0) java.math.RoundingMode.HALF_DOWN else java.math.RoundingMode.HALF_UP)
    return java.text.NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = digits
        minimumFractionDigits = 0
        isGroupingUsed = false
    }.format(rounded)
}
