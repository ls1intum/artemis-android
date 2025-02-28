package de.tum.informatics.www1.artemis.native_app.core.common.app_version


data class NormalizedAppVersion(private val versionName: String): Comparable<NormalizedAppVersion> {

    init {
        require(versionName.matches(Regex("^\\d+\\.\\d+\\.\\d+$"))) {
            "Version string must be in the format 'major.minor.patch'"
        }
    }

    private val versionParts: List<Int> = versionName.split(".").map { it.toInt() }

    val major: Int = versionParts.getOrNull(0) ?: 0
    val minor: Int = versionParts.getOrNull(1) ?: 0
    val patch: Int = versionParts.getOrNull(2) ?: 0

    override fun toString(): String = versionName

    override fun compareTo(other: NormalizedAppVersion): Int {
        return when {
            major != other.major -> major - other.major
            minor != other.minor -> minor - other.minor
            else -> patch - other.patch
        }
    }

    companion object {
        fun fromFullVersionName(versionName: String): NormalizedAppVersion {
            return NormalizedAppVersion(versionName.substringBefore("-").trim())
        }

        val ZERO = NormalizedAppVersion("0.0.0")

        fun fromNullable(versionName: String?): NormalizedAppVersion {
            return versionName?.let { NormalizedAppVersion(it) } ?: ZERO
        }
    }
}