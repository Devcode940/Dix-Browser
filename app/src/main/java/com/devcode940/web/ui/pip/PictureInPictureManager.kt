package com.devcode940.web.ui.pip

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import android.widget.VideoView
import androidx.annotation.RequiresApi

/**
 * Minimal Picture-in-Picture helper. Enters PiP mode with a 16:9 aspect ratio.
 */
class PictureInPictureManager(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun enterPictureInPictureMode(@Suppress("UNUSED_PARAMETER") videoView: VideoView? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            (context as? android.app.Activity)?.enterPictureInPictureMode(params)
        }
    }
}
