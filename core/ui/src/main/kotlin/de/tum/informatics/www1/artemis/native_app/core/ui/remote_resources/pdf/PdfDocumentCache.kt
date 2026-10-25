package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/** Local authenticated copies shared within an open reader's modes. Each new open uses a new key
 * to fetch current bytes. Active readers and recent share grants are retained; all other files
 * obey count, byte and age limits. No URL/token is stored. */
internal class PdfDocumentCache(
    private val directory: File,
    private val maxEntries: Int = 8,
    private val maxBytes: Long = 256L * 1024 * 1024,
    private val now: () -> Long = System::currentTimeMillis
) {
    private val downloadMutex = Mutex()
    private val active = mutableMapOf<String, Int>()
    private val invalid = mutableSetOf<String>()

    class Lease internal constructor(val file: File, private val release: () -> Unit) {
        private val closed = AtomicBoolean(false)
        fun close() { if (closed.compareAndSet(false, true)) release() }
    }

    suspend fun acquire(key: String, load: suspend (File) -> Unit): Lease = downloadMutex.withLock {
        directory.mkdirs()
        val hash = MessageDigest.getInstance("SHA-256").digest(key.toByteArray()).joinToString("") { "%02x".format(it) }
        val target = File(directory, "$hash.pdf")
        val existing = synchronized(this) {
            if (target.exists() && target.length() > 0 && target.name !in invalid) {
                target.setLastModified(now())
                lease(target).also { prune() }
            } else null
        }
        if (existing != null) return@withLock existing
        val temporary = File.createTempFile("loading-", ".part", directory)
        try {
            load(temporary)
            check(temporary.length() > 0L) { "Empty PDF response" }
            synchronized(this) {
                check(temporary.renameTo(target)) { "Cannot retain PDF" }
                invalid.remove(target.name)
                target.setLastModified(now())
                lease(target).also { prune() }
            }
        } finally { temporary.delete() }
    }

    @Synchronized
    private fun lease(file: File): Lease {
        active[file.name] = (active[file.name] ?: 0) + 1
        return Lease(file) {
            synchronized(this) {
                val count = (active[file.name] ?: 1) - 1
                if (count <= 0) active.remove(file.name) else active[file.name] = count
                prune()
            }
        }
    }

    @Synchronized
    fun retain(file: File): Lease? = if (file.parentFile == directory && file.exists()) lease(file) else null

    /** Android gives no completion callback for a receiving app's URI read. Retain for 24 hours,
     * including across this process dying; the marker stores an expiry timestamp only. */
    @Synchronized
    fun retainForShare(file: File) {
        if (file.parentFile == directory && file.exists()) File(directory, "${file.name}.share").writeText((now() + RETENTION_MS).toString())
    }

    @Synchronized
    fun invalidate(file: File) {
        if (file.parentFile == directory) invalid += file.name
        prune()
    }

    @Synchronized
    fun prune() {
        val timestamp = now()
        val files = directory.listFiles().orEmpty().filter { it.extension == "pdf" }.sortedBy { it.lastModified() }.toMutableList()
        fun pinned(file: File): Boolean = (active[file.name] ?: 0) > 0 ||
            (File(directory, "${file.name}.share").takeIf { it.exists() }?.let { marker -> runCatching { marker.readText().toLongOrNull() }.getOrNull() } ?: 0L) > timestamp
        fun remove(file: File) {
            file.delete()
            File(directory, "${file.name}.share").delete()
            invalid.remove(file.name)
            files.remove(file)
        }
        files.toList().filter { !pinned(it) && (it.name in invalid || timestamp - it.lastModified() >= RETENTION_MS) }.forEach(::remove)
        while (files.size > maxEntries || files.sumOf { it.length() } > maxBytes) {
            val oldest = files.firstOrNull { !pinned(it) } ?: break
            remove(oldest)
        }
        directory.listFiles().orEmpty().filter { it.extension == "part" && timestamp - it.lastModified() >= RETENTION_MS }.forEach { it.delete() }
    }

    companion object {
        private const val RETENTION_MS = 24L * 60 * 60 * 1000
        private val caches = ConcurrentHashMap<String, PdfDocumentCache>()
        fun shared(cacheDirectory: File): PdfDocumentCache = caches.getOrPut(cacheDirectory.absolutePath) {
            PdfDocumentCache(File(cacheDirectory, "lecture-pdfs")).also {
                // Retire copies produced by the previous viewer after existing share grants age out.
                cacheDirectory.listFiles().orEmpty().filter { file ->
                    file.name.startsWith("lecture-") && file.extension == "pdf" && System.currentTimeMillis() - file.lastModified() >= RETENTION_MS
                }.forEach { file -> file.delete() }
            }
        }
    }
}
