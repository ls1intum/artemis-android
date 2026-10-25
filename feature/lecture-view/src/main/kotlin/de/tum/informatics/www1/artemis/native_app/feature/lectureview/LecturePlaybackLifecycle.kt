package de.tum.informatics.www1.artemis.native_app.feature.lectureview

/** Owns the foreground playback resource and never reads a released player. */
internal class LecturePlaybackLifecycle(
    private val position: () -> Long,
    private val savePosition: (Long) -> Unit,
    private val pausePlayer: () -> Unit,
    private val releasePlayer: () -> Unit
) {
    private var closed = false
    fun save() {
        if (!closed) savePosition(position().coerceAtLeast(0))
    }
    fun pause() {
        if (!closed) {
            save()
            pausePlayer()
        }
    }
    fun close() {
        if (!closed) {
            pause()
            closed = true
            releasePlayer()
        }
    }
}
