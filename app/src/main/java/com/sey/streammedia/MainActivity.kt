package com.sey.streammedia

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.arthurivanets.bottomsheets.BaseBottomSheet
import com.arthurivanets.bottomsheets.ktx.showActionPickerBottomSheet
import com.arthurivanets.bottomsheets.sheets.listeners.OnItemSelectedListener
import com.arthurivanets.bottomsheets.sheets.model.Option
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener , Player.EventListener {
    companion object {
        const val TAG = "mainactivity"
    }

    private var HI_BITRATE:Long = 2097152
    private var MI_BITRATE:Long = 1048576
    private var LO_BITRATE:Long = 524288
    private var qualitiyBitrate:Long=0
    private var defaultBandwidthMeter: DefaultBandwidthMeter?=null
    private var adaptiveTrackSelection: TrackSelection.Factory?=null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var bottomSheetOption: BaseBottomSheet? = null
    private var bottomSheetChooseResolution: BaseBottomSheet? = null

    private var player: SimpleExoPlayer? = null
    private var isHideStatusBar: Boolean = false
    private var fullscreen = false
    private var isHdMedia = false
    private var isLock = false
    private var isPotrait = false
    private var index = 0
    private lateinit var btnScale: ImageButton
    private lateinit var btnLock: ImageButton
    private lateinit var hdMedia: ImageView
    private lateinit var btnUnlock: ImageButton
    private lateinit var btnRotate: ImageButton
    private lateinit var controllerMedia: LinearLayout
    private lateinit var btnOption: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = playerView.findViewById<Toolbar>(R.id.toolbar_controller)
        setSupportActionBar(toolbar)
        requestFullScreenIfLandscape()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)

        btnScale = playerView.findViewById(R.id.btn_scale)
        btnLock = playerView.findViewById(R.id.btn_lock)
        hdMedia = playerView.findViewById(R.id.hd_media)
        btnUnlock = playerView.findViewById(R.id.btn_unlock)
        btnRotate = playerView.findViewById(R.id.btn_rotate)
        controllerMedia = playerView.findViewById(R.id.container_control_media)
        btnOption = playerView.findViewById(R.id.btn_option)

        btnScale.setOnClickListener {
            if (index < 3) {
                when (index) {
                    0 -> {
                        setAspectRatio(getString(R.string.stretch))
                    }
                    1 -> {
                        setAspectRatio(getString(R.string.fit))
                    }
                    2 -> {
                        setAspectRatio(getString(R.string.original))
                    }
                }
                index += 1
            } else {
                index = 0
            }
        }
        hdMedia.setOnClickListener {
            if (isHdMedia) {
                hdMedia.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_hd_gray))
                isHdMedia = false
            } else {
                hdMedia.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_hd))
                isHdMedia = true
            }
        }
        btnUnlock.setOnClickListener {
            playerView.showController()
            controllerMedia.visibility = View.VISIBLE
            toolbar.visibility = View.VISIBLE
            btnUnlock.visibility = View.GONE
        }
        btnLock.setOnClickListener {
            playerView.hideController()
            btnUnlock.visibility = View.VISIBLE
            controllerMedia.visibility = View.GONE
            toolbar.visibility = View.GONE
        }
        btnRotate.setOnClickListener {
            Log.d(TAG,"click")
            if (isPotrait) {
                isPotrait = false
                Log.d(TAG,"landscape")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            } else {
                isPotrait = true
                Log.d(TAG,"portrait")
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        }
        btnOption.setOnClickListener {
//            bottomSheetOption=SheetOption(this).also { bottomSheetOption!!.show() }
            showActionsBottomSheet()
        }


//        layout_overlay_controller.setOnClickListener{
//            Log.d(TAG, "click")
//
//            if (isHideStatusBar==false){
//                // Hide the status bar.
//                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//                actionBar?.hide()
//                isHideStatusBar=true
//            }else{
//                // Hide the status bar.
//                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//                actionBar?.show()
//                isHideStatusBar=false
//
//            }
//        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
        return HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(uri)
    }

    private fun initializePlayer() {
        adaptiveTrackSelection= AdaptiveTrackSelection.Factory(defaultBandwidthMeter)
        player = ExoPlayerFactory.newSimpleInstance(this,
            DefaultRenderersFactory(this),
            DefaultTrackSelector(adaptiveTrackSelection),
            DefaultLoadControl()
        )
        playerView.player = player
        player!!.playWhenReady = playWhenReady;
        player!!.seekTo(currentWindow, playbackPosition);
        player!!.prepare(buildMediaSource(Uri.parse(getString(R.string.link_media))),false,false)
        player!!.addListener(this)

    }


    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }


    private fun requestFullScreenIfLandscape() {
        if (resources.getBoolean(R.bool.landscape)) {
            isPotrait=false
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }else{
            isPotrait=true
        }
    }

    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    override fun onStart() {
        super.onStart()
        hideSystemUi()
        if (Util.SDK_INT >= 24) {
            initializePlayer();

            Log.d("mainactivity", "onStart $currentWindow $playbackPosition")
        }
    }

    override fun onPause() {
        super.onPause()
//        if (Util.SDK_INT < 24) {
//            releasePlayer()
        Log.d("mainactivity", "onPause $currentWindow $playbackPosition")
//        }


    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
            Log.d("mainactivity", "onResume $currentWindow $playbackPosition")
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
            Log.d("mainactivity", "onStop $currentWindow $playbackPosition")
        }


    }

    override fun onClick(v: View?) {

    }

    private fun setQualityMedia(bitrate:Long){
        releasePlayer()
        defaultBandwidthMeter = DefaultBandwidthMeter.Builder()
            .setInitialBitrateEstimate(bitrate)
            .build()
        adaptiveTrackSelection= AdaptiveTrackSelection.Factory(defaultBandwidthMeter)
        player = ExoPlayerFactory.newSimpleInstance(this,
            DefaultRenderersFactory(this),
            DefaultTrackSelector(adaptiveTrackSelection),
            DefaultLoadControl()
        )
        playerView.player = player
        player!!.playWhenReady = playWhenReady;
        player!!.seekTo(currentWindow, playbackPosition);
        player!!.prepare(buildMediaSource(Uri.parse(getString(R.string.link_media))),false,false)
        player!!.addListener(this)
        Log.d(TAG,"setQuality $")
    }



    private fun showActionSetQualityMedia() {
        bottomSheetChooseResolution =
            showActionPickerBottomSheet(options = getActionOptions("choose_quality"),

                onItemSelectedListener = OnItemSelectedListener {
                    Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                    when (it.title) {
                        "Automatic" -> {
                            releasePlayer()
                            initializePlayer()
                        }
                        "Full HD 1080P" -> {
                            setQualityMedia(HI_BITRATE)
                        }
                        "HD 720P" -> {
                            setQualityMedia(MI_BITRATE)
                        }
                        "480P" -> {
                            setQualityMedia(LO_BITRATE)
                        }
                    }
                }
            )
    }

    private fun showActionsBottomSheet() {
        bottomSheetOption = showActionPickerBottomSheet(
            options = getActionOptions("option_more"),
            onItemSelectedListener = OnItemSelectedListener {
                Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                when (it.title) {
                    "Report" -> {

                    }
                    "Quality" -> {
                        showActionSetQualityMedia()
                    }
                    "Help & feedback" -> {

                    }
                }
            }
        )
    }

    private fun getActionOptions(typeDialog: String): List<Option> {
        // your options

        return if (typeDialog == "option_more") {
            ArrayList<Option>().apply {
                add(0, Option().setIconId(R.drawable.ic_baseline_flag_24).setTitle("Report"))
                add(1, Option().setIconId(R.drawable.ic_baseline_settings_24).setTitle("Quality"))
                add(
                    2,
                    Option().setIconId(R.drawable.ic_baseline_help_24).setTitle("Help & feedback")
                )
            }
        } else {
            ArrayList<Option>().apply {
                add(0, Option().setTitle("Automatic"))
                add(1, Option().setTitle("Full HD 1080P"))
                add(2, Option().setTitle("HD 720P"))
                add(3, Option().setTitle("480P"))
            }
        }
    }

    private fun setAspectRatio(type: String) {
        when (type) {
            getString(R.string.fit) -> {
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
            getString(R.string.crop) -> {
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
            getString(R.string.stretch) -> {

                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

            }
            getString(R.string.original) -> {
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            }
        }
    }
    private fun getBitrateMediaStream(): Int {
        return player!!.videoFormat.bitrate
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        super.onTimelineChanged(timeline, manifest, reason)
        Log.d(TAG, "onTimeLine $timeline , $manifest , $reason")
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        super.onTracksChanged(trackGroups, trackSelections)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        super.onLoadingChanged(isLoading)
        if (isLoading){
            progress_bar.visibility=View.VISIBLE
        }else{
            progress_bar.visibility=View.GONE
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super.onRepeatModeChanged(repeatMode)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        Log.d(TAG,"playstate $playWhenReady $playbackState")
            if (playbackState==3){
                progress_bar.visibility=View.GONE
            }

    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        super.onPlayerError(error)
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        super.onShuffleModeEnabledChanged(shuffleModeEnabled)
    }

    override fun onSeekProcessed() {
        super.onSeekProcessed()
        progress_bar.visibility=View.VISIBLE
    }
}