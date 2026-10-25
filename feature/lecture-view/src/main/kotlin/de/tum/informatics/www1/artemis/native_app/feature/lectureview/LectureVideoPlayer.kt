@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.authTokenOrEmptyString
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.media.*
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreR

/** One foreground player per open unit. Reopening and foregrounding always wait for user play. */
@Composable
internal fun LectureVideoPlayer(url: String, identity: String, onDismiss: () -> Unit) {
    val session by LocalArtemisContextProvider.current.collectArtemisContextAsState()
    val account = (session as? ArtemisContext.LoggedIn)?.loginName.orEmpty()
    val scope = "$account|${normalizedMediaOrigin(session.serverUrl)}"
    val openedScope = remember { scope }
    if (scope != openedScope) {
        LaunchedEffect(scope) { onDismiss() }
        return
    }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val store = remember(context) { MediaProgressStore(context) }
    val progressKey = remember(scope, identity, url) { mediaProgressKey(account, session.serverUrl, identity, url) }
    val kind = remember(url) { classifyMediaSource(url) }
    val browser = LocalLinkOpener.current
    var foreground by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    var fullscreen by rememberSaveable { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) foreground = false
            if (event == Lifecycle.Event.ON_START) foreground = true
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().padding(12.dp)) {
                if (!fullscreen) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(CoreR.string.lecture_media_close)) }
                }
                if (kind == MediaSourceKind.INVALID) {
                    Text(stringResource(CoreR.string.lecture_media_invalid))
                } else {
                    key(progressKey, attempt, session.authTokenOrEmptyString) {
                        if (foreground) {
                            if (kind == MediaSourceKind.WEB) {
                                Text(stringResource(CoreR.string.lecture_media_provider))
                            } else {
                                NativeLectureVideo(
                                    url, kind, session.serverUrl, session.authTokenOrEmptyString,
                                    progressKey, store,
                                    Modifier.weight(1f).fillMaxWidth(),
                                    onRetry = { attempt++ },
                                    onFullscreen = { fullscreen = !fullscreen }
                                )
                                TextButton(onClick = { fullscreen = !fullscreen }) {
                                    Text(stringResource(if (fullscreen) CoreR.string.lecture_media_exit_fullscreen else CoreR.string.lecture_media_fullscreen))
                                }
                            }
                        } else Spacer(Modifier.weight(1f))
                    }
                    if (kind == MediaSourceKind.WEB) Text(stringResource(CoreR.string.lecture_media_provider), style = MaterialTheme.typography.bodySmall)
                    if (!fullscreen) TextButton(onClick = { browser.openLink(url) }) { Text(stringResource(CoreR.string.lecture_media_browser)) }
                }
            }
        }
    }
}

@Composable
private fun NativeLectureVideo(
    url: String,
    kind: MediaSourceKind,
    serverUrl: String,
    token: String,
    progressKey: String,
    store: MediaProgressStore,
    modifier: Modifier,
    onRetry: () -> Unit,
    onFullscreen: () -> Unit
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    val player = remember(url, token) {
        val factory = OkHttpDataSource.Factory(mediaHttpClient(serverUrl, token))
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(factory))
            .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                setMediaItem(MediaItem.Builder().setUri(url).setMimeType(if (kind == MediaSourceKind.HLS) "application/x-mpegURL" else "video/mp4").build())
                seekTo(store.read(progressKey) ?: 0L)
                playWhenReady = false
            }
    }
    val ownership = remember(player) {
        LecturePlaybackLifecycle({ player.currentPosition }, { store.save(progressKey, it) }, player::pause, player::release)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, ownership) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE) ownership.pause() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(exception: PlaybackException) { error = true; loading = false }
            override fun onPlaybackStateChanged(state: Int) { loading = state == Player.STATE_BUFFERING }
        }
        player.addListener(listener)
        player.prepare()
        onDispose {
            player.removeListener(listener)
            ownership.close()
        }
    }
    // Persist during playback as well as before dismissal/background/rotation.
    LaunchedEffect(player) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            ownership.save()
        }
    }
    Box(modifier.background(androidx.compose.ui.graphics.Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { PlayerView(it).apply {
                this.player = player
                setShowSubtitleButton(true)
                setFullscreenButtonClickListener { onFullscreen() }
                controllerShowTimeoutMs = 5000
            } },
            onRelease = { it.player = null }
        )
        if (loading && !error) CircularProgressIndicator(Modifier.align(androidx.compose.ui.Alignment.Center))
        if (error) MediaFailure(Modifier.fillMaxSize(), onRetry)
    }
}

@Composable
private fun MediaFailure(modifier: Modifier, onRetry: () -> Unit) {
    Column(modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp), verticalArrangement = Arrangement.Center) {
        Text(stringResource(CoreR.string.lecture_media_failed))
        Text(stringResource(CoreR.string.lecture_media_access_recovery), style = MaterialTheme.typography.bodySmall)
        Button(onClick = onRetry) { Text(stringResource(CoreR.string.lecture_media_retry)) }
    }
}
