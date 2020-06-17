package com.sey.mediaview

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.arthurivanets.bottomsheets.BaseBottomSheet
import com.arthurivanets.bottomsheets.ktx.showActionPickerBottomSheet
import com.arthurivanets.bottomsheets.sheets.listeners.OnItemSelectedListener
import com.arthurivanets.bottomsheets.sheets.model.Option
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlaybackControlView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import kotlinx.android.synthetic.main.activity_main.*

class MediaViewer : AppCompatActivity(), View.OnClickListener, Player.EventListener {
    companion object {
        const val TAG = "mainactivity"
        private var getDataLink = ""
    }

    private var isDoubleCliked = false
    private var handler: Handler = Handler()

    private var HI_BITRATE: Long = 50000000
    private var MI_BITRATE: Long = 2800000
    private var LO_BITRATE: Long = 1400000
    private var LOWEST_BITRATE: Long = 1000000
    private var maxBandwidthMeter:BandwidthMeter?=null
    private var defaultBandwidthMeter: DefaultBandwidthMeter? = null
    private var adaptiveTrackSelection: TrackSelection.Factory? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var bottomSheetOption: BaseBottomSheet? = null
    private var bottomSheetChooseResolution: BaseBottomSheet? = null
    private var valueResolution: String = ""
    private var currentIndexResolution: Int = 0
    private var indexAspectRatio:Int=0

    private var player: SimpleExoPlayer? = null
    private var isPotrait = false

    private var layoutReplay: LinearLayout? = null
    private var layoutForward: LinearLayout? = null
    private var containerController: LinearLayout? = null
    private var txtSeekTo: TextView? = null
    private var btnScale:ImageView?=null

    private lateinit var controllerMedia: LinearLayout
    private lateinit var btnOption: ImageView
    private lateinit var txtShowCurrentResolution: TextView
    fun setUrl(url: String) {
        Log.d(TAG, "datamedia $url")
        getDataLink = url
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val toolbar = playerView.findViewById<Toolbar>(R.id.toolbar_controller)
//        setSupportActionBar(toolbar)
        requestFullScreenIfLandscape()
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        Log.d(TAG, "datamediaThatget $getDataLink")

        btnScale=playerView.findViewById(R.id.btn_scale)
//        txtSeekTo = playerView.findViewById(R.id.txt_seekTo)
//        controllerMedia = playerView.findViewById(R.id.container_control_media)
//        containerController = playerView.findViewById(R.id.container_controller)
//        btnOption = playerView.findViewById(R.id.btn_option)
//        layoutReplay = playerView.findViewById(R.id.layout_replay)
//        layoutForward = playerView.findViewById(R.id.layout_forward)
//        playerView.controllerHideOnTouch = false
//        playerView.controllerShowTimeoutMs = 1000
//        featureDoubleClick()
//        initComponentClick()

    }

    private fun initComponentClick() {
        btnOption.setOnClickListener {
            showActionsBottomSheet()
        }
        btnScale!!.setOnClickListener {
            when (indexAspectRatio) {
                0 -> {
                    indexAspectRatio+=1
                    setAspectRatio(0)
                }
                1 -> {
                    indexAspectRatio+=1
                    setAspectRatio(1)
                }
                2 -> {
                    indexAspectRatio+=1
                    setAspectRatio(2)
                }
                3 ->{
                    indexAspectRatio=0
                    setAspectRatio(0)
                }
            }
        }
    }


    private fun buildMediaSource(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))
        return HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(uri)

    }

    private fun initializePlayer() {
        adaptiveTrackSelection = AdaptiveTrackSelection.Factory(defaultBandwidthMeter)
        player = ExoPlayerFactory.newSimpleInstance(
            this,
            DefaultRenderersFactory(this),
            DefaultTrackSelector(adaptiveTrackSelection),
            DefaultLoadControl()
        )
        playerView.player = player
        player!!.playWhenReady = playWhenReady
        player!!.seekTo(currentWindow, playbackPosition)
        player!!.prepare(MediaSourceBuilder().build(Uri.parse(getDataLink)), false, false)
        player!!.addListener(this)
        player!!.addVideoListener(object : VideoListener {
            override fun onVideoSizeChanged(
                width: Int,
                height: Int,
                unappliedRotationDegrees: Int,
                pixelWidthHeightRatio: Float
            ) {
                super.onVideoSizeChanged(
                    width,
                    height,
                    unappliedRotationDegrees,
                    pixelWidthHeightRatio
                )
//                txtShowCurrentResolution.text=height.toString()+"P"
                Log.d(TAG, "initial $height $width")
            }
        })
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
//            isPotrait = false
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
//            hideSystemUi()
            Log.d(TAG, "rotate true")
            hideSystemUi()
        } else {
            isPotrait = true
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


    override fun onClick(v: View?) {

    }

    private fun setQualityMedia(bitrate: Long) {
        releasePlayer()
        defaultBandwidthMeter = DefaultBandwidthMeter.Builder()
            .setInitialBitrateEstimate(bitrate)
            .build()
        adaptiveTrackSelection = AdaptiveTrackSelection.Factory(defaultBandwidthMeter)
        player = ExoPlayerFactory.newSimpleInstance(
            this,
            DefaultRenderersFactory(this),
            DefaultTrackSelector(adaptiveTrackSelection),
            DefaultLoadControl()
        )
        playerView.player = player
        player!!.playWhenReady = playWhenReady;
        player!!.seekTo(currentWindow, playbackPosition);
        player!!.prepare(MediaSourceBuilder().build(Uri.parse(getDataLink)), false, false)
        player!!.addListener(this)
        player!!.addVideoListener(object : VideoListener {
            override fun onVideoSizeChanged(
                width: Int,
                height: Int,
                unappliedRotationDegrees: Int,
                pixelWidthHeightRatio: Float
            ) {
                super.onVideoSizeChanged(
                    width,
                    height,
                    unappliedRotationDegrees,
                    pixelWidthHeightRatio
                )
//                txtShowCurrentResolution.text=height.toString()+"P"
                Log.d(TAG, "height $width, $pixelWidthHeightRatio")
            }
        })
        player!!.addAnalyticsListener(object : AnalyticsListener {
            override fun onBandwidthEstimate(
                eventTime: AnalyticsListener.EventTime?,
                totalLoadTimeMs: Int,
                totalBytesLoaded: Long,
                bitrateEstimate: Long
            ) {
                super.onBandwidthEstimate(
                    eventTime,
                    totalLoadTimeMs,
                    totalBytesLoaded,
                    bitrateEstimate
                )
                Log.d(TAG, "bandwidth $eventTime ,$bitrateEstimate")
            }

            override fun onLoadingChanged(
                eventTime: AnalyticsListener.EventTime?,
                isLoading: Boolean
            ) {
                super.onLoadingChanged(eventTime, isLoading)
                Log.d(TAG, "onLoading $eventTime ,$isLoading")
            }

            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime?,
                loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
                mediaLoadData: MediaSourceEventListener.MediaLoadData?
            ) {
                super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
                Log.d(TAG, "onLoadingChange ${loadEventInfo!!.responseHeaders}")
            }

            override fun onVideoSizeChanged(
                eventTime: AnalyticsListener.EventTime?,
                width: Int,
                height: Int,
                unappliedRotationDegrees: Int,
                pixelWidthHeightRatio: Float
            ) {
                super.onVideoSizeChanged(
                    eventTime,
                    width,
                    height,
                    unappliedRotationDegrees,
                    pixelWidthHeightRatio
                )
                Log.d(TAG, "onVideoSizeChanged ${eventTime!!.mediaPeriodId!!.isAd}")
            }
        })
        Log.d(TAG, "setQuality $")
    }


    private fun showActionSetQualityMedia(checkQuality: Int, countHideQuality: Int) {
        val bottomSheetChooseQualitiy = ChooseQualityBottomSheet.getInstance()
        bottomSheetChooseQualitiy.setValueForCheckVisible(checkQuality)
        if (countHideQuality != 0) {
            for (i in 1..countHideQuality) {
                bottomSheetChooseQualitiy.setHideLayout(i)
            }
        }
        bottomSheetChooseQualitiy.registerCallback(object :
            ChooseQualityBottomSheet.callBackChooseQuality {
            override fun callAuto() {
                if (checkQuality != 0) {
                    releasePlayer()
                    initializePlayer()
                    currentIndexResolution = 0
                }
                hideSystemUi()
            }

            override fun call1080() {
                if (checkQuality != 1) {
                    setQualityMedia(HI_BITRATE)
                    currentIndexResolution = 1

                }
                hideSystemUi()
            }

            override fun call720() {
                if (checkQuality != 2) {
                    setQualityMedia(MI_BITRATE)
                    currentIndexResolution = 2
                }
                hideSystemUi()
            }

            override fun call480() {
                if (checkQuality != 3) {
                    setQualityMedia(LO_BITRATE)
                    currentIndexResolution = 3
                }
                hideSystemUi()
            }

            override fun call360() {
                if (checkQuality != 4) {
                    setQualityMedia(LOWEST_BITRATE)
                    currentIndexResolution = 4
                }
                hideSystemUi()
            }

        })
        bottomSheetChooseQualitiy.show(this.supportFragmentManager, "")
    }

    private fun showActionsBottomSheet() {
        bottomSheetOption = showActionPickerBottomSheet(
            options = getActionOptions("option_more", "hd"),
            onItemSelectedListener = OnItemSelectedListener {
                Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                when (it.title) {
                    "Share" -> {

                    }
                    "Quality" -> {
//                        Log.d(TAG,"qualitymedia ${getCurrentQualityMedia()}")
                        showActionSetQualityMedia(currentIndexResolution, 0)
                    }
                    "Report" -> {

                    }
                }
            }
        )
    }


    private fun getActionOptions(typeDialog: String, currentQuality: String): List<Option> {
        // your options
        return ArrayList<Option>().apply {
            add(0, Option().setIconId(R.drawable.ic_baseline_flag_24).setTitle("Report"))
            add(1, Option().setIconId(R.drawable.ic_baseline_settings_24).setTitle("Quality"))
            add(2, Option().setIconId(R.drawable.ic_baseline_help_24).setTitle("Help & feedback"))
        }

    }


    private fun setAspectRatio(type: Int) {
        Log.d(TAG, "type screen $type")
        when (type) {
            0 -> {
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                getString(R.string.fit)
            }

            1-> {

                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                getString(R.string.stretch)
            }
            2-> {
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                getString(R.string.original)
            }
        }
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        super.onTimelineChanged(timeline, manifest, reason)
        Log.d(TAG, "onTimeLine $timeline , $manifest , $reason")
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
        super.onTracksChanged(trackGroups, trackSelections)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        super.onLoadingChanged(isLoading)
        if (isLoading) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super.onRepeatModeChanged(repeatMode)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        Log.d(TAG, "playstate $playWhenReady $playbackState")
        if (playbackState == Player.STATE_READY) {
            progress_bar.visibility = View.GONE
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
        progress_bar.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        hideSystemUi()
//        requestFullScreenIfLandscape()
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
        requestFullScreenIfLandscape()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
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


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
//        val orientation = newConfig.orientation
//        if (orientation == Configuration.ORIENTATION_PORTRAIT){
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        }
//        else if (orientation == Configuration.ORIENTATION_LANDSCAPE){
//
//        }

    }

    private fun featureDoubleClick() {
        var isHide = false
        containerController!!.setOnClickListener {

//                if (isHide==true){
//                    playerView.showController()
//                    isHide=false
//                }else{
//                    playerView.hideController()
//                    isHide=true
//                }
            if (playerView.isControllerVisible) {
                playerView.hideController()
            } else {
                playerView.showController()
            }

        }
        val r = Runnable {
            //Actions when Single Clicked
            isDoubleCliked = false;
            if (playerView.isControllerVisible) {
                playerView.hideController()
            } else {
                playerView.showController()
            }
        }
        layoutForward!!.setOnClickListener {

            if (isDoubleCliked) {
                //Actions when double Clicked
                Log.d(TAG, "forward")
                val nextPosition = player?.currentPosition?.plus(30000)
                player!!.seekTo(nextPosition!!)
                txtSeekTo!!.text = "Forward 30s"
                txtSeekTo!!.animate().alpha(1f)
                Handler().postDelayed({
                    txtSeekTo!!.animate().alpha(0.0f)
                }, 1000)
                Log.d(TAG, "nextposition ${player!!.currentPosition}")
                isDoubleCliked = false;
                //remove callbacks for Handlers
                handler.removeCallbacks(r);
            } else {
                isDoubleCliked = true;
                handler.postDelayed(r, 500);
            }
        }
        layoutReplay!!.setOnClickListener {
            if (isDoubleCliked) {
                //Actions when double Clicked
                Log.d(TAG, "forward")
                val replayPosition = player?.currentPosition?.minus(10000)
                txtSeekTo!!.text = "Replay 10s"
                txtSeekTo!!.animate().alpha(1f)
                Handler().postDelayed({
                    txtSeekTo!!.animate().alpha(0.0f)
                }, 1000)
                player!!.seekTo(replayPosition!!)
                isDoubleCliked = false;
                //remove callbacks for Handlers
                handler.removeCallbacks(r);
            } else {
                isDoubleCliked = true;
                handler.postDelayed(r, 500);
            }
        }
    }
}

