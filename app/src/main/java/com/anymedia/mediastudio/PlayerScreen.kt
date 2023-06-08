package com.anymedia.mediastudio

import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isInvisible
import androidx.core.view.size
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView


@Composable
fun PlayerScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var player: ExoPlayer? = null
    var playWhenReady = true
    var mediaItemIndex = 0
    var playbackPosition = 0L

    val ctx = LocalContext.current
    val playerView = PlayerView(ctx)
    player = ExoPlayer.Builder(ctx)
        .build()

    DisposableEffect(key1 = lifecycleOwner) {
        val lifecycleEventObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    val mediaItem = MediaItem.fromUri(ctx.getString(R.string.media_url_mp4))
                    player?.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
                    player?.playWhenReady = playWhenReady
                    player?.prepare()
                    playerView.onResume()
                }

                Lifecycle.Event.ON_STOP -> {
                    playerView.onPause()
                    player?.let { exoPlayer ->
                        playbackPosition = exoPlayer.currentPosition
                        mediaItemIndex = exoPlayer.currentMediaItemIndex
                        playWhenReady = exoPlayer.playWhenReady
                        exoPlayer.release()
                    }
                    player = null
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = player
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            it.player = player
        }
    }
}