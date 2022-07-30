package com.example.videoplayer

import android.content.ContentValues.TAG
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*
import java.util.concurrent.TimeUnit


class VideoPlayerActivity : AppCompatActivity(), View.OnClickListener,
    OnPreparedListener, MediaPlayer.OnErrorListener, OnCompletionListener, OnSeekBarChangeListener {
    private var currentPlayingTime: Int = 0
    private var isVideoCompleted: Boolean = false
    private var myVideoView: VideoView? = null
    private var playPauseBtn: ImageButton? = null
    private var forwardBtn: ImageButton? = null
    private var backwardBtn: ImageButton? = null
    private var seekBar: SeekBar? = null
    private var playingTimeTextView: TextView? = null
    private var fullTimeTextView: TextView? = null
    private var isVideoPaused = false
    private var seekProgress: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private var hideController: Runnable? = null
    private var videoControllerConstraintLayout: ConstraintLayout? = null
    private var videoPlayerRelativeLayout: RelativeLayout? = null
    private var isUserTrackingSeekbar = false
    private var fileFromListUri: Uri? = null
    private var isVideoCancelled: Boolean = false
    private var videoFullDurationTime: Int = 0
    private var fileFromStorageUri: Uri? = null
    private val SAVED_INSTANCE_CURRENT_POSITION_KEY = "545"
    private val SAVED_INSTANCE_ISPAUSED_KEY = "5t5"
    private var recoveredLastVideoPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        init()
        if (intent.getStringExtra(R.string.EXTRA_VIDEO_KEY.toString()) != null) {
            fileFromListUri = Uri.parse(intent.getStringExtra(R.string.EXTRA_VIDEO_KEY.toString()))
            myVideoView!!.setVideoURI(fileFromListUri)
        } else {
            fileFromStorageUri = intent.data
            myVideoView!!.setVideoURI(fileFromStorageUri)
        }
        myVideoView!!.setOnPreparedListener(this)
        myVideoView!!.setOnErrorListener(this)
        myVideoView!!.setOnCompletionListener(this)

    }

    fun init() {
        myVideoView = findViewById(R.id.my_video_view)
        forwardBtn = findViewById(R.id.button_forward)
        backwardBtn = findViewById(R.id.button_backward)
        playPauseBtn = findViewById(R.id.button_play_pause)
        seekBar = findViewById(R.id.videoPlayer_seekbar)
        fullTimeTextView = findViewById(R.id.textView_fullTime)
        playingTimeTextView = findViewById(R.id.textView_playingTime)
        videoControllerConstraintLayout = findViewById(R.id.constraintLayout_videoController)
        videoPlayerRelativeLayout = findViewById(R.id.relativeLayout_videoController)
        videoPlayerRelativeLayout?.setOnClickListener(this)
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        videoFullDurationTime = mediaPlayer.duration
        forwardBtn!!.setOnClickListener(this)
        backwardBtn!!.setOnClickListener(this)
        playPauseBtn!!.setOnClickListener(this)
        seekBar!!.setOnSeekBarChangeListener(this)
        if (!isVideoCompleted && !isVideoPaused) {
            mediaPlayer.start()
            playPauseBtn!!.setImageResource(R.drawable.ic_baseline_pause_24)
        }
        fullTimeTextView?.text = longToTime(mediaPlayer.duration.toLong())
        playingTimeTextView?.text = longToTime(mediaPlayer.currentPosition.toLong())

        seekBar!!.max = mediaPlayer.duration
        initRunnable(mediaPlayer)
        if (recoveredLastVideoPosition>0){
            myVideoView?.seekTo(recoveredLastVideoPosition)
            seekBar!!.setProgress(recoveredLastVideoPosition)
            playingTimeTextView?.text = longToTime(mediaPlayer.currentPosition.toLong())
        }
    }

    private fun initRunnable(mp: MediaPlayer) {
        seekProgress = object : Runnable {
            override fun run() {
                if (!isVideoPaused && !isUserTrackingSeekbar && !isVideoCancelled && !isVideoCompleted) {
                    try {
                        currentPlayingTime = myVideoView?.currentPosition!!
                        seekBar!!.progress = currentPlayingTime
                        playingTimeTextView!!.text =
                            longToTime(myVideoView!!.currentPosition.toLong())

                    } catch (e: Exception) {
                        Log.d(TAG, "run: ${e.message}")
                    }
                }
                handler.postDelayed(this, 50)
            }
        }
        handler.postDelayed(seekProgress as Runnable, 50)
        hideController = Runnable {
            if (!isVideoCompleted && !isVideoCancelled && !isVideoPaused && (myVideoView?.isPlaying == true)) {
                videoControllerConstraintLayout!!.visibility = View.INVISIBLE
            }
        }
        handler.postDelayed(hideController as Runnable, 3000)
    }

    override fun onError(mediaPlayer: MediaPlayer, i: Int, i1: Int): Boolean {

        return true
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        if (fileFromListUri != null) {
            myVideoView?.setVideoURI(fileFromListUri)
        } else {
            myVideoView?.setVideoURI(fileFromStorageUri)
        }
        isVideoCompleted = true
        isVideoPaused = true
        playPauseBtn?.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        seekBar?.progress = 0
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {

        if (b) {
            startHidingTheControllerTimer()
            myVideoView!!.seekTo(i)
            seekBar.progress = i
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        isUserTrackingSeekbar = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        isUserTrackingSeekbar = false
    }

    override fun onClick(view: View) {
        findClickedView(view)
    }

    private fun findClickedView(view: View) {
        when (view.id) {
            R.id.button_play_pause -> if (myVideoView!!.isPlaying) {
                currentPlayingTime = myVideoView!!.currentPosition
                myVideoView!!.pause()
                playPauseBtn!!.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                isVideoPaused = true
                handler.removeCallbacks(hideController!!)
            } else if (isVideoPaused) {
                myVideoView!!.start()
                playPauseBtn!!.setImageResource(R.drawable.ic_baseline_pause_24)
                isVideoPaused = false
                isVideoCompleted = false
                startHidingTheControllerTimer()
            }
            R.id.button_forward -> {
                myVideoView!!.seekTo(myVideoView!!.currentPosition + 5000)
                startHidingTheControllerTimer()
            }
            R.id.button_backward -> {
                myVideoView!!.seekTo(myVideoView!!.currentPosition - 5000)
                startHidingTheControllerTimer()
            }
            R.id.relativeLayout_videoController ->
                if (videoControllerConstraintLayout!!.visibility == View.INVISIBLE) {
                    videoControllerConstraintLayout!!.visibility = View.VISIBLE
                    if (myVideoView!!.isPlaying) {
                        startHidingTheControllerTimer()
                    }
                }
        }
    }

    private fun startHidingTheControllerTimer() {
        handler.removeCallbacks(hideController!!)
        handler.postDelayed(hideController!!, 3000)
    }

    override fun onBackPressed() {
        isVideoCancelled = true
        super.onBackPressed()
    }

    private fun longToTime(duration: Long): String {

        return String.format(
            Locale.ENGLISH, "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(duration),
            TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    duration
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    duration
                )
            )
        )
    }

    override fun onStop() {
        hideController?.let { handler.removeCallbacks(it); handler.removeCallbacks(it) }
        isVideoPaused = true
        isVideoCancelled = true
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_INSTANCE_CURRENT_POSITION_KEY, currentPlayingTime)
        outState.putBoolean(SAVED_INSTANCE_ISPAUSED_KEY,isVideoPaused)
        Log.d(TAG, "onSaveInstanceState: $currentPlayingTime")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        recoveredLastVideoPosition = savedInstanceState.getInt(SAVED_INSTANCE_CURRENT_POSITION_KEY)
        isVideoPaused = savedInstanceState.getBoolean(SAVED_INSTANCE_ISPAUSED_KEY)
        Log.d(TAG, "onRestoreInstanceState: $recoveredLastVideoPosition")
    }
}