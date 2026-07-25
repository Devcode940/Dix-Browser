package com.devcode940.web.ui.pip

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.MediaController
import android.widget.VideoView

/**
 * Enhanced PiP Video Player with Media Controller
 */
class PiPVideoPlayer(
    private val context: Context,
    private val videoView: VideoView
) {

    private var mediaSession: MediaSessionCompat? = null
    private var mediaController: MediaController? = null

    fun setupMediaController() {
        mediaController = MediaController(context)
        mediaController?.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Setup MediaSession for PiP controls
        setupMediaSession()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(context, "DixBrowserPiP").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                    .build()
            )

            setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Video")
                    .build()
            )

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    videoView.start()
                }

                override fun onPause() {
                    videoView.pause()
                }

                override fun onSkipToNext() {
                    // Can be extended for playlist
                }

                override fun onSkipToPrevious() {
                    // Can be extended
                }
            })

            isActive = true
        }
    }

    fun playVideo(uri: Uri) {
        videoView.setVideoURI(uri)
        videoView.start()
        
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
                .build()
        )
    }

    fun pauseVideo() {
        videoView.pause()
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0f)
                .build()
        )
    }

    fun release() {
        mediaSession?.release()
        mediaSession = null
        mediaController = null
    }

    fun enterPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // This should be called from Activity
        }
    }
}