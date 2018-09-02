package com.dailymotion.android.player.sampleapp

import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout

import com.dailymotion.android.player.sdk.PlayerWebView
import kotlinx.android.synthetic.main.sample_activity.*

import java.util.HashMap
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MOVIE
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.CastContext




class SampleActivity : AppCompatActivity(), View.OnClickListener {

    val VIDEO_XID = "x26hv6c"
    private var mFullscreen = false
    private var sessionManager: SessionManager? = null
    var chromecastConnected = true

    private inner class SessionManagerListenerImpl : SessionManagerListener<Session> {
        override fun onSessionResumeFailed(p0: Session?, p1: Int) {
        }

        override fun onSessionSuspended(p0: Session?, p1: Int) {
        }

        override fun onSessionStarting(p0: Session?) {
        }

        override fun onSessionResuming(p0: Session?, p1: String?) {
        }

        override fun onSessionEnding(p0: Session?) {
        }

        override fun onSessionStartFailed(p0: Session?, p1: Int) {
        }

        override fun onSessionStarted(session: Session, sessionId: String) {
            dm_player_web_view.pause()
            chromecastConnected = true
        }

        override fun onSessionResumed(session: Session, wasSuspended: Boolean) {
        }

        override fun onSessionEnded(session: Session, error: Int) {
            dm_player_web_view.play()
            chromecastConnected = false
        }
    }

    fun chromecastLoad(xid: String) {
        val castSession = CastContext.getSharedInstance(this).sessionManager.currentSession as CastSession

        val movieMetadata = MediaMetadata(MEDIA_TYPE_MOVIE)

        movieMetadata.putString(MediaMetadata.KEY_TITLE, "Video Title")
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, "Video Subtitle")

        val mediaInfo = MediaInfo.Builder(xid)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(movieMetadata)
                .build()
        val remoteMediaClient = castSession.getRemoteMediaClient()

        val mediaLoadOptions = MediaLoadOptions.Builder()
                .build()
        remoteMediaClient.load(mediaInfo, mediaLoadOptions)
    }

    fun chromecastStop() {
        val castSession = CastContext.getSharedInstance(this).sessionManager.currentSession as CastSession
        castSession.remoteMediaClient.stop()
    }

    fun chromecastPlay() {
        val castSession = CastContext.getSharedInstance(this).sessionManager.currentSession as CastSession
        castSession.remoteMediaClient.play()
    }

    fun chromecastPause() {
        val castSession = CastContext.getSharedInstance(this).sessionManager.currentSession as CastSession
        castSession.remoteMediaClient.pause()
    }

    fun chromecastSeek(millis: Long) {
        val castSession = CastContext.getSharedInstance(this).sessionManager.currentSession as CastSession
        castSession.remoteMediaClient.seek(millis)
    }

    fun onFullScreenToggleRequested() {
        setFullScreenInternal(!mFullscreen)
        val params: LinearLayout.LayoutParams

        if (mFullscreen) {
            toolbar!!.visibility = View.GONE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        } else {
            toolbar!!.visibility = View.VISIBLE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (215 * resources.displayMetrics.density).toInt())
        }
        dm_player_web_view.layoutParams = params
    }

    private fun setFullScreenInternal(fullScreen: Boolean) {
        mFullscreen = fullScreen
        if (mFullscreen) {
            action_layout.visibility = View.GONE
        } else {
            action_layout.visibility = View.VISIBLE
        }

        dm_player_web_view.setFullscreenButton(mFullscreen)
    }

    private val sessionManagerListener = SessionManagerListenerImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sample_activity)

        sessionManager = CastContext.getSharedInstance(this).getSessionManager()

        setSupportActionBar(toolbar)
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), media_route_button);

        if (toolbar != null) {
            toolbar!!.visibility = View.VISIBLE
            toolbar!!.setBackgroundColor(resources.getColor(android.R.color.background_dark))
            toolbar!!.setTitleTextColor(resources.getColor(android.R.color.white))

            val actionBar = supportActionBar
            actionBar?.setTitle(getString(R.string.app_name))
        }

        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            dm_player_web_view.setIsWebContentsDebuggingEnabled(true)
        }

        val playerParams = HashMap<String, String>()
        dm_player_web_view.load(VIDEO_XID, playerParams as Map<String, Any>?)

        dm_player_web_view.setEventListener { event, map ->
            when (event) {
                "apiready" -> log("apiready")
                "start" -> log("start")
                "loadedmetadata" -> log("loadedmetadata")
                "progress" -> log(event + " (bufferedTime: " + dm_player_web_view.bufferedTime + ")")
                "durationchange" -> log(event + " (duration: " + dm_player_web_view.duration + ")")
                "timeupdate", "ad_timeupdate", "seeking", "seeked" -> log(event + " (currentTime: " + dm_player_web_view.position + ")")
                "video_start", "ad_start", "ad_play", "playing", "end" -> log(event + " (ended: " + dm_player_web_view.isEnded + ")")
                "ad_pause", "ad_end", "video_end", "play", "pause" -> log(event + " (paused: " + dm_player_web_view.videoPaused + ")")
                "qualitychange" -> log(event + " (quality: " + dm_player_web_view.quality + ")")
                PlayerWebView.EVENT_VOLUMECHANGE -> log(event + " (volume: " + dm_player_web_view.volume + ")")
                PlayerWebView.EVENT_FULLSCREEN_TOGGLE_REQUESTED -> onFullScreenToggleRequested()
                else -> {
                }
            }
        }

        val playButton = findViewById<View>(R.id.btnTogglePlay) as Button
        playButton.setOnClickListener(this@SampleActivity)
        val togglePlayButton = findViewById<View>(R.id.btnPlay) as Button
        togglePlayButton.setOnClickListener(this@SampleActivity)
        val pauseButton = findViewById<View>(R.id.btnPause) as Button
        pauseButton.setOnClickListener(this@SampleActivity)

        val seekButton = findViewById<View>(R.id.btnSeek) as Button
        seekButton.setOnClickListener(this@SampleActivity)
        val loadVideoButton = findViewById<View>(R.id.btnLoadVideo) as Button
        loadVideoButton.setOnClickListener(this@SampleActivity)
        val setQualityButton = findViewById<View>(R.id.btnSetQuality) as Button
        setQualityButton.setOnClickListener(this@SampleActivity)
        val setSubtitleButton = findViewById<View>(R.id.btnSetSubtitle) as Button
        setSubtitleButton.setOnClickListener(this@SampleActivity)

        val toggleControlsButton = findViewById<View>(R.id.btnToggleControls) as Button
        toggleControlsButton.setOnClickListener(this@SampleActivity)
        val showControlsButton = findViewById<View>(R.id.btnShowControls) as Button
        showControlsButton.setOnClickListener(this@SampleActivity)
        val hideControlsButton = findViewById<View>(R.id.btnHideControls) as Button
        hideControlsButton.setOnClickListener(this@SampleActivity)
        val setVolumeButton = findViewById<Button>(R.id.btnSetVolume)
        setVolumeButton.setOnClickListener(this@SampleActivity)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btnPlay) {
            if (chromecastConnected) {
                chromecastPlay()
            } else {
                dm_player_web_view.play()
            }
        } else if (v.id == R.id.btnTogglePlay) {
            dm_player_web_view.togglePlay()
        } else if (v.id == R.id.btnPause) {
            if (chromecastConnected) {
                chromecastPause()
            } else {
                dm_player_web_view.pause()
            }
        } else if (v.id == R.id.btnSeek) {
            if (chromecastConnected) {
                chromecastSeek(30000)
            } else {
                dm_player_web_view.seek(30.0)
            }
        } else if (v.id == R.id.btnLoadVideo) {
            if (chromecastConnected) {
                chromecastLoad(VIDEO_XID)
            } else {
                dm_player_web_view.load(VIDEO_XID)
            }
        } else if (v.id == R.id.btnSetQuality) {
            dm_player_web_view.quality = "240"
        } else if (v.id == R.id.btnSetSubtitle) {
            dm_player_web_view.setSubtitle("en")
        } else if (v.id == R.id.btnToggleControls) {
            dm_player_web_view.toggleControls()
        } else if (v.id == R.id.btnShowControls) {
            dm_player_web_view.showControls(true)
        } else if (v.id == R.id.btnHideControls) {
            dm_player_web_view.showControls(false)
        } else if (v.id == R.id.btnSetVolume) {
            val text = (findViewById<View>(R.id.editTextVolume) as EditText).text.toString()
            val volume = java.lang.Float.parseFloat(text)
            dm_player_web_view.volume = volume
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.sample, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        dm_player_web_view.goBack()
    }

    override fun onPause() {
        super.onPause()

        dm_player_web_view.onPause()
        sessionManager?.removeSessionManagerListener(sessionManagerListener);
    }

    override fun onResume() {
        super.onResume()

        dm_player_web_view.onResume()
        sessionManager?.addSessionManagerListener(sessionManagerListener);
    }

    private fun log(text: String) {
        if (action_layout.visibility == View.GONE) {
            return
        }

        logText!!.append("\n" + text)
        val scroll = logText!!.layout.getLineTop(logText!!.lineCount) - logText!!.height
        if (scroll > 0) {
            logText!!.scrollTo(0, scroll)
        } else {
            logText!!.scrollTo(0, 0)
        }
    }
}
