@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureVideoPlayer(url: String, identity: String, onDismiss: () -> Unit, title: String = "", onReloadSource: (() -> Unit)? = null) {
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
    val activity = context as? android.app.Activity
    val originalOrientation by rememberSaveable { mutableStateOf(activity?.requestedOrientation ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }
    val fullscreenController = remember(activity, originalOrientation) { activity?.let { LectureFullscreenController(it, originalOrientation) } }
    DisposableEffect(fullscreenController) { onDispose { fullscreenController?.close() } }
    LaunchedEffect(fullscreen) { fullscreenController?.setFullscreen(fullscreen) }
    BackHandler {
        if (fullscreenController != null) fullscreenController.back({ fullscreen = false }, onDismiss)
        else if (fullscreen) fullscreen = false else onDismiss()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("media_video_page"),
        containerColor = androidx.compose.ui.graphics.Color.Black,
        contentWindowInsets = if (fullscreen) WindowInsets(0, 0, 0, 0) else WindowInsets.safeDrawing,
        topBar = {
            if (!fullscreen) TopAppBar(
                title = { Text(title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                navigationIcon = { NavigationBackButton(onNavigateBack = onDismiss) },
                actions = {
                    IconButton(onClick = { fullscreen = true }) { Icon(Icons.Default.Fullscreen, stringResource(CoreR.string.lecture_media_fullscreen)) }
                    IconButton(onClick = { browser.openLink(url) }) { Icon(Icons.Default.OpenInBrowser, stringResource(CoreR.string.lecture_media_browser)) }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (kind == MediaSourceKind.INVALID) Text(stringResource(CoreR.string.lecture_media_invalid), color = MaterialTheme.colorScheme.onSurface)
            else key(progressKey, attempt, session.authTokenOrEmptyString) {
                if (foreground) NativeLectureVideo(
                    url, kind, session.serverUrl, session.authTokenOrEmptyString,
                    progressKey, store, fullscreen, Modifier.fillMaxSize(),
                    onRetry = { if (onReloadSource != null) onReloadSource() else attempt++ },
                    onFullscreen = { fullscreen = it }
                )
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
    fullscreen: Boolean,
    modifier: Modifier,
    onRetry: () -> Unit,
    onFullscreen: (Boolean) -> Unit
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
        LecturePlayerView(player, fullscreen, Modifier.fillMaxSize(), onFullscreen)
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

/** Keep Media3's own fullscreen control in the same state as the surrounding page. */
@Composable
internal fun LecturePlayerView(player: Player?, fullscreen: Boolean, modifier: Modifier, onFullscreen: (Boolean) -> Unit) {
    AndroidView(
        modifier = modifier,
        factory = { PlayerView(it).apply {
            this.player = player
            setShowSubtitleButton(true)
            setFullscreenButtonClickListener { onFullscreen(it) }
            controllerShowTimeoutMs = 5000
        } },
        update = { it.setFullscreenButtonState(fullscreen) },
        onRelease = { it.player = null }
    )
}
