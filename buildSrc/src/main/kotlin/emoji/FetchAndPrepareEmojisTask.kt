package emoji

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

abstract class FetchAndPrepareEmojisTask : DefaultTask() {

    @get:Input
    abstract val commit: Property<String>

    @get:Input
    abstract val set: Property<String>

    @get:Input
    abstract val outputDir: Property<File>

    @TaskAction
    fun performAction() {
        val outputFolder = outputDir.get()
        outputFolder.mkdirs()
        val outputFile = outputFolder.resolve("emojis.json")

        if (outputFile.exists()) {
            logger.info("Skipping generation of emoji file as it already exists.")
            return
        }

        val ktorClient = HttpClient {}

        val json = Json {
            ignoreUnknownKeys = true
        }

        val data: GithubEmojiJson = runBlocking {
            val bodyAsText: String = ktorClient.get("https://raw.githubusercontent.com/missive/emoji-mart/") {
                url {
                    appendPathSegments(
                        commit.get(),
                        "packages",
                        "emoji-mart-data",
                        "sets",
                        set.get(),
                        "native.json"
                    )
                }
            }.body()

            json.decodeFromString(bodyAsText)
        }

        val entries: List<OutputEmojiEntry> = data
            .emojis
            .values
            .mapNotNull { emojiData ->
                val id = emojiData.id
                val unicode = emojiData.skins.firstOrNull()?.native ?: return@mapNotNull null

                OutputEmojiEntry(id, unicode)
            }

        outputFile.writeText(json.encodeToString(entries))
    }
}