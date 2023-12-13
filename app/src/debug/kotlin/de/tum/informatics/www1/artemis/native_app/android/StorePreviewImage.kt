package de.tum.informatics.www1.artemis.native_app.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview(device = "spec:width=1024px,height=500px,dpi=440")
private fun StorePreviewImage() {
    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    contentScale = FixedScale(1f)
                )

                Column {
                    Text(
                        text = "Artemis",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = "Interactive Learning with Individual Feedback",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}