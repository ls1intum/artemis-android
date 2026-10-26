package de.tum.informatics.www1.artemis.native_app.android.ui

import androidx.lifecycle.ViewModel
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile

/** An open page's validated bytes, retained across all same-process Activity recreation. */
internal class OpenedPdfPage : ViewModel() {
    private var document: PdfFile? = null
    private var owner: AutoCloseable? = null
    val hasSnapshot: Boolean
        get() = document?.hasRetainedDocument == true

    fun getOrCreate(create: () -> PdfFile): PdfFile =
        document
            ?: create().also {
                document = it
                owner = it.retainOpenDocument()
            }

    override fun onCleared() {
        owner?.close()
        owner = null
        document = null
    }
}
