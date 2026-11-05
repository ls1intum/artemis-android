package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.security.MessageDigest

@Serializable
internal data class TextExerciseServerVersion(
    val id: Int? = null,
    val text: String = "",
    val submitted: Boolean = false
)

@Serializable
internal data class TextExerciseDraft(
    val text: String,
    val base: TextExerciseServerVersion? = null
)

internal data class TextExerciseDraftKey(
    val server: String,
    val account: String,
    val exerciseId: Long,
    val participationId: Long
)

internal interface TextExerciseDraftStore {
    val coordinator: TextExerciseEditingCoordinator
    suspend fun load(key: TextExerciseDraftKey, owner: String): TextExerciseDraft?

    /** Enqueued immediately; completion and disk writes survive the editor's ViewModel. */
    fun save(key: TextExerciseDraftKey, owner: String, draft: TextExerciseDraft): Deferred<Boolean>
}

/** Keeps requests single-flight across navigation, until their transport completes. */
internal class TextExerciseEditingCoordinator(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) {
    private val owners = mutableMapOf<TextExerciseDraftKey, String>()
    private val lock = Any()
    private val _submissions = MutableStateFlow<Set<TextExerciseDraftKey>>(emptySet())
    val submissions = _submissions.asStateFlow()

    fun claim(key: TextExerciseDraftKey, owner: String) = synchronized(lock) { owners[key] = owner }
    fun owns(key: TextExerciseDraftKey, owner: String): Boolean = synchronized(lock) { owners[key] == owner }

    fun launchSubmission(key: TextExerciseDraftKey, owner: String, block: suspend () -> Unit): Boolean {
        synchronized(lock) {
            if (owners[key] != owner || key in _submissions.value) return false
            _submissions.value += key
        }
        scope.launch {
            try { block() }
            finally { synchronized(lock) { _submissions.value -= key } }
        }
        return true
    }
}

/** One app-wide owner serializes reads and writes, including across editor recreation. */
internal class FileTextExerciseDraftStore(
    private val directory: File,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    override val coordinator: TextExerciseEditingCoordinator = TextExerciseEditingCoordinator()
) : TextExerciseDraftStore {
    private val operations = Channel<() -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch { for (operation in operations) operation() }
    }

    override suspend fun load(key: TextExerciseDraftKey, owner: String): TextExerciseDraft? = enqueue {
        coordinator.claim(key, owner)
        val file = file(key)
        if (!file.exists()) null else Json.decodeFromString<TextExerciseDraft>(file.readText(Charsets.UTF_8))
    }.await()

    override fun save(key: TextExerciseDraftKey, owner: String, draft: TextExerciseDraft): Deferred<Boolean> = enqueue {
        if (!coordinator.owns(key, owner)) return@enqueue false
        check(directory.isDirectory || directory.mkdirs()) { "Cannot create draft directory" }
        val destination = file(key)
        val temporary = File(destination.path + ".tmp")
        try {
            FileOutputStream(temporary).use { output ->
                output.write(Json.encodeToString(draft).toByteArray(Charsets.UTF_8))
                output.fd.sync()
            }
            // AtomicFile logs some rename/sync failures instead of throwing; report them to the editor.
            Files.move(temporary.toPath(), destination.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            true
        } finally {
            temporary.delete()
        }
    }

    private fun file(key: TextExerciseDraftKey): File {
        // Length prefixes keep account/server boundaries unambiguous. No credential is persisted.
        val identity = listOf(key.server.trimEnd('/'), key.account, key.exerciseId.toString(), key.participationId.toString())
            .joinToString("") { "${it.length}:$it" }
        val name = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(directory, "$name.json")
    }

    private fun <T> enqueue(block: () -> T): Deferred<T> {
        val result = CompletableDeferred<T>()
        check(operations.trySend {
            try { result.complete(block()) }
            catch (exception: Exception) { result.completeExceptionally(exception) }
        }.isSuccess)
        return result
    }
}
