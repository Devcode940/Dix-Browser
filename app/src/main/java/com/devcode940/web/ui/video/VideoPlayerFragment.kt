package com.devcode940.web.ui.video

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.devcode940.web.databinding.FragmentVideoPlayerBinding
import com.devcode940.web.ui.pip.PiPVideoPlayer

/**
 * Dedicated Video Player Fragment with Media Controller and PiP support
 */
class VideoPlayerFragment : Fragment() {

    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding!!

    private var videoUri: Uri? = null
    private var pipVideoPlayer: PiPVideoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoUri = it.getParcelable(ARG_VIDEO_URI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupVideoPlayer()
        setupControls()
    }

    private fun setupVideoPlayer() {
        pipVideoPlayer = PiPVideoPlayer(requireContext(), binding.videoView)
        pipVideoPlayer?.setupMediaController()

        videoUri?.let { uri ->
            pipVideoPlayer?.playVideo(uri)
        }
    }

    private fun setupControls() {
        binding.btnEnterPip.setOnClickListener {
            activity?.let { act ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val params = android.app.PictureInPictureParams.Builder()
                        .setAspectRatio(android.util.Rational(16, 9))
                        .build()
                    act.enterPictureInPictureMode(params)
                }
            }
        }

        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pipVideoPlayer?.release()
        _binding = null
    }

    companion object {
        private const val ARG_VIDEO_URI = "video_uri"

        fun newInstance(videoUri: Uri): VideoPlayerFragment {
            return VideoPlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_VIDEO_URI, videoUri)
                }
            }
        }
    }
}