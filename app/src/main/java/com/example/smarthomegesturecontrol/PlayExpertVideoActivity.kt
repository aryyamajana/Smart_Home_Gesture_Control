package com.example.smarthomegesturecontrol

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class PlayExpertVideoActivity : AppCompatActivity() {
    private var chosenGesture: String? = null
    private var expertVideoView: VideoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_expertvideo)
        expertVideoView = findViewById(R.id.gestureVideo)

        // get the chosen Gesture from Main screen
        val intent = intent
        chosenGesture = intent.getStringExtra("gesture_name")
        gestureToBePlayed = "h_" + chosenGesture?.replace(" ".toRegex(), "_")?.toLowerCase(Locale.ROOT)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            expertVideoView?.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        ExpertVideoPlayerRelease()
    }

    private fun initializePlayer() {
        val expertVideoUri = getExpertVideo(gestureToBePlayed)
        expertVideoView?.setVideoURI(expertVideoUri)
        expertVideoView?.start()
    }

    private fun getExpertVideo(mediaName: String?): Uri? {
        return Uri.parse("android.resource://" + packageName +
                "/raw/" + mediaName)
    }

    private fun ExpertVideoPlayerRelease() {
        expertVideoView?.stopPlayback()
    }

    fun replayExpertVideo(view: View?) {
        initializePlayer()
    }

    fun moveToPracticeRecordScreen(view: View?) {
        val practiceRecordIntent = Intent(this@PlayExpertVideoActivity, PracticeRecordActivity::class.java)
        practiceRecordIntent.putExtra("gesture_name", chosenGesture)
        startActivity(practiceRecordIntent)
    }

    companion object {
        private var gestureToBePlayed: String? = null
    }
}