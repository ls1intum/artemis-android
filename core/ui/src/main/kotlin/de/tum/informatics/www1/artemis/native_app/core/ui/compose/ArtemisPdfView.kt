package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rizzi.bouquet.HorizontalPDFReader
import com.rizzi.bouquet.HorizontalPdfReaderState
import com.rizzi.bouquet.ResourceType
import com.rizzi.bouquet.VerticalPDFReader
import com.rizzi.bouquet.VerticalPdfReaderState
import com.rizzi.bouquet.rememberHorizontalPdfReaderState
import com.rizzi.bouquet.rememberVerticalPdfReaderState
import io.ktor.http.HttpHeaders

// Inspired by: https://github.com/GRizzi91/bouquet
/*
 *   Copyright [2022] [Graziano Rizzi]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

@Composable
fun ArtemisPdfView(
    modifier: Modifier,
    filename: String? = null,
    url: String,
    authToken: String,
) {
    val isVertical = remember { mutableStateOf(true) }
    val resource = ResourceType.Remote(
        url = url,
        headers = hashMapOf(HttpHeaders.Cookie to "jwt=$authToken")
    )

    val verticalPdfState = rememberVerticalPdfReaderState(
        resource = resource,
        isZoomEnable = true
    )
    val horizontalPdfState = rememberHorizontalPdfReaderState(
        resource = resource,
        isZoomEnable = true
    )

    val pdfState = if (isVertical.value) verticalPdfState else horizontalPdfState

    Box(
        modifier = modifier.padding(8.dp),
    ) {
        Column {
            pdfState.file?.let {
                Text(
                    text = filename ?: it.name,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (isVertical.value) {
                VerticalPDFReader(
                    state = pdfState as VerticalPdfReaderState,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface)
                        .fillMaxSize()
                )
            } else {
                HorizontalPDFReader(
                    state = pdfState as HorizontalPdfReaderState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                )
            }
        }

        FloatingActionButton(
            onClick = {
                isVertical.value = !isVertical.value
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isVertical.value) Icons.AutoMirrored.Filled.RotateLeft else Icons.AutoMirrored.Filled.RotateRight,
                contentDescription = null
            )
        }

//        FloatingActionButton(
//            onClick = { /* Share logic */ },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .zIndex(1f)
//                .padding(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Share,
//                contentDescription = null
//            )
//        }

        if (!pdfState.isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { pdfState.loadPercent / 100f },
                    modifier = Modifier.height(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart).padding(top = 58.dp, end = 8.dp)
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "Page: ${pdfState.currentPage}/${pdfState.pdfPageCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}