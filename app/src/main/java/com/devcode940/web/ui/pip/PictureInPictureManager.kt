package com.devcode940.web.ui.pip

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.widget.VideoView
import androidx.annotation.RequiresApi

/**
 * Picture-in-Picture Video Manager with Media Controller
 */
class PictureInPictureManager(private val context: Context) {

    private var pipVideoPlayer: PiPVideoPlayer? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun enterPictureInPictureMode(videoView: VideoView? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()

            // If a VideoView is provided, set it up with media controller
            videoView?.let {
                pipVideoPlayer = PiPVideoPlayer(context, it)
                pipVideoPlayer?.setupMediaController()
            }

            // Request PiP mode
            (context as? android.app.Activity)?.enterPictureInPictureMode(params)
        }
    }

    fun playVideoInPiP(videoView: VideoView, uri: Uri) {
        pipVideoPlayer = PiPVideoPlayer(context, videoView)
        pipVideoPlayer?.setupMediaController()
        pipVideoPlayer?.playVideo(uri)
    }

    fun release() {
        pipVideoPlayer?.release()
        pipVideoPlayer = null
    }
}