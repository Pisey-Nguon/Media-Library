package com.sey.mediaview

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.net.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.tubitv.ui.TubiLoadingView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MediaViewer : AppCompatActivity(), View.OnClickListener, Player.EventListener{
    companion object {
        const val TAG = "mainactivity"
        private var getDataLink = ""
    }


    private var playerState = false
    private var connectivityNetwork = false
    private var isClickTimeBar = false
    private var isBuffering = false
    private var isFullscreen = false
    private var isDoubleCliked = false
    private var handler: Handler = Handler()

    private var HI_BITRATE: Long = 50000000
    private var MI_BITRATE: Long = 2800000
    private var LO_BITRATE: Long = 1400000
    private var LOWEST_BITRATE: Long = 1000000
    private var durationMedia: Long = 0
    private var durationSet: Boolean = false
    private var mPlayerControlView:PlayerControlView?=null
    private var maxBandwidthMeter: BandwidthMeter? = null
    private var defaultBandwidthMeter: DefaultBandwidthMeter? = null
    private var adaptiveTrackSelection: TrackSelection.Factory? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var bottomSheetOption: BaseBottomSheet? = null
    private var bottomSheetChooseResolution: BaseBottomSheet? = null
    private var valueResolution: String = ""
    private var currentIndexResolution: Int = 0
    private var currentIndexPlayBackSpeed: Int = 3
    private var indexAspectRatio: Int = 0
    private var mRatioAlreadyCalculated = false
    private var mVideoWidthHeightRatio: Float = 0f

    private var player: SimpleExoPlayer? = null
    private var isPotrait = false

    private var progressBar: TubiLoadingView? = null
    private var btnPlay: ImageView? = null
    private var btnPause: ImageView? = null
    private var btnReplay: ImageView? = null
    private var layoutBtnControllerPlay: LinearLayout? = null
    private var layoutReplay: LinearLayout? = null
    private var layoutForward: LinearLayout? = null
    private var containerController: LinearLayout? = null
    private var defaultTimeBar: DefaultTimeBar? = null
    private var txtTimeProgress:TextView?=null
    private var txtTimeDuration:TextView?=null
    private var txtSeekTo: TextView? = null
    private var txtNextSeek: TextView? = null
    private var txtPreviewSeek: TextView? = null
    private var btnScale: ImageView? = null
    private var imgFullOrExit: ImageView? = null
    private var btnFullOrExit: LinearLayout? = null

    private lateinit var controllerMedia: LinearLayout
    private lateinit var btnOption: ImageView
    private lateinit var txtShowCurrentResolution: TextView
    fun setUrl(url: String) {
        Log.d(TAG, "datamedia $url")
        getDataLink = url
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlackGray)
        Log.d(TAG, "datamediaThatget $getDataLink")
        txtTimeProgress=playerView.findViewById(R.id.exo_position)
        txtTimeDuration=playerView.findViewById(R.id.exo_duration)

//        btnScale=playerView.findViewById(R.id.btn_scale)
        progressBar = playerView.findViewById(R.id.progress_bar)
        btnPlay = playerView.findViewById(R.id.btn_play)
        btnPause = playerView.findViewById(R.id.btn_pause)
        btnReplay = playerView.findViewById(R.id.btn_replay)
        layoutBtnControllerPlay = playerView.findViewById(R.id.layout_btn_controller_play)
        txtSeekTo = playerView.findViewById(R.id.txt_seekTo)
        controllerMedia = playerView.findViewById(R.id.container_control_media)
        containerController = playerView.findViewById(R.id.container_controller)
        defaultTimeBar = playerView.findViewById(R.id.exo_progress)
        btnOption = playerView.findViewById(R.id.btn_option)
        layoutReplay = playerView.findViewById(R.id.layout_replay)
        layoutForward = playerView.findViewById(R.id.layout_forward)
        btnFullOrExit = playerView.findViewById(R.id.btn_full_or_exit)
        imgFullOrExit = playerView.findViewById(R.id.img_full_or_exit)
        txtNextSeek = playerView.findViewById(R.id.next_seek)
        txtPreviewSeek = playerView.findViewById(R.id.preview_seek)
//        playerView.controllerHideOnTouch = false
//        playerView.controllerShowTimeoutMs = 1000
        val cm = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(ConnectivityCallback())




        featureDoubleClick()
        initComponentClick()
        initComponentCallBackOfView()

    }

    private fun initComponentCallBackOfView() {

    }

    private fun initComponentClick() {
        btnOption.setOnClickListener {
            showActionsBottomSheet()
        }
//        btnScale!!.setOnClickListener {
//            when (indexAspectRatio) {
//                0 -> {
//                    indexAspectRatio+=1
//                    setAspectRatio(0)
//                }
//                1 -> {
//                    indexAspectRatio+=1
//                    setAspectRatio(1)
//                }
//                2 -> {
//                    indexAspectRatio+=1
//                    setAspectRatio(2)
//                }
//                3 ->{
//                    indexAspectRatio=0
//                    setAspectRatio(0)
//                }
//            }
//        }
        btnFullOrExit!!.setOnClickListener {
            setFullScreen()
        }
        btnPlay!!.setOnClickListener {
            startPlayer()
            btnPlay!!.visibility = View.GONE
            btnPause!!.visibility = View.VISIBLE

        }
        btnPause!!.setOnClickListener {
            pausePlayer()
            btnPlay!!.visibility = View.VISIBLE
            btnPause!!.visibility = View.GONE

        }

        defaultTimeBar!!.addListener(object : TimeBar.OnScrubListener {

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                isClickTimeBar = true
            }

            override fun onScrubStart(timeBar: TimeBar, position: Long) {

            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {

            }

        })

        btnReplay!!.setOnClickListener {
            initializePlayer()
        }
        txtTimeProgress!!.addOnLayoutChangeListener(object :View.OnLayoutChangeListener{
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                if (txtTimeProgress!!.text == txtTimeDuration!!.text) {
                    pausePlayer()
                    showBtnReplay()
                }
            }

        })
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
            showSystemUI()
            Log.d(TAG, "rotate false")
            isPotrait = true
        }

    }

//    private fun hideSystemUi() {
//        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
//    }


    override fun onClick(v: View?) {

    }

    private fun setQualityMedia(bitrate: Long) {
        releasePlayer()
        defaultBandwidthMeter = DefaultBandwidthMeter.Builder(this)
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
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun call1080() {
                if (checkQuality != 1) {
                    setQualityMedia(HI_BITRATE)
                    currentIndexResolution = 1

                }
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun call720() {
                if (checkQuality != 2) {
                    setQualityMedia(MI_BITRATE)
                    currentIndexResolution = 2
                }
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun call480() {
                if (checkQuality != 3) {
                    setQualityMedia(LO_BITRATE)
                    currentIndexResolution = 3
                }
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun call360() {
                if (checkQuality != 4) {
                    setQualityMedia(LOWEST_BITRATE)
                    currentIndexResolution = 4
                }
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun onDismiss() {
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

        })
        bottomSheetChooseQualitiy.show(this.supportFragmentManager, "")
    }

    private fun showActionSetPlayBackSpeed(checkTypeSpeed: Int) {
        val choosePlayBackSpeed = ChoosePlayBackSpeed.getInstance()
        choosePlayBackSpeed.setValueForCheckVisible(checkTypeSpeed)
        choosePlayBackSpeed.registerCallback(object :
            ChoosePlayBackSpeed.callBackChoosePlayBackSpeed {
            override fun speed0() {
                playBackSpeed(0.25f)
                currentIndexPlayBackSpeed = 0
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun speed1() {
                playBackSpeed(0.5f)
                currentIndexPlayBackSpeed = 1
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun speed2() {
                playBackSpeed(0.75f)
                currentIndexPlayBackSpeed = 2
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun speed3() {
                playBackSpeed(1f)
                currentIndexPlayBackSpeed = 3
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun speed4() {
                playBackSpeed(1.25f)
                currentIndexPlayBackSpeed = 4
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun speed5() {
                playBackSpeed(1.5f)
                currentIndexPlayBackSpeed = 5
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun speed6() {
                playBackSpeed(1.75f)
                currentIndexPlayBackSpeed = 6
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun speed7() {
                playBackSpeed(2f)
                currentIndexPlayBackSpeed = 7
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

            override fun onDismiss() {
                if (isFullscreen) {
                    hideSystemUi()
                }
            }

        })
        choosePlayBackSpeed.show(this.supportFragmentManager, "")
    }

    private fun playBackSpeed(speed: Float) {
        val params = PlaybackParameters(speed)
        player!!.playbackParameters = params
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
                    "Captions" -> {

                    }
                    "PlayBack speed" -> {
                        showActionSetPlayBackSpeed(currentIndexPlayBackSpeed)
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
            add(
                2,
                Option().setIconId(R.drawable.ic_baseline_closed_caption_24).setTitle("Captions")
            )
            add(
                3,
                Option().setIconId(R.drawable.ic_baseline_av_timer_24).setTitle("PlayBack speed")
            )
            add(4, Option().setIconId(R.drawable.ic_baseline_help_24).setTitle("Help & feedback"))
        }

    }


    private fun setAspectRatio(type: Int) {
        Log.d(TAG, "type screen $type")
        when (type) {
            0 -> {
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                getString(R.string.fit)
            }

            1 -> {

                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                getString(R.string.stretch)
            }
            2 -> {
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                getString(R.string.original)
            }
        }
    }

    private fun setHeightPlayerToMatchParent() {
        val params = playerView.layoutParams
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        playerView.layoutParams = params
    }

    private fun setFullScreen() {
        if (isFullscreen) {
            showSystemUI()

            imgFullOrExit!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.exo_controls_fullscreen_enter
                )
            )
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            if (supportActionBar != null) {
                supportActionBar!!.show()
            }

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val params: ViewGroup.LayoutParams = playerView.layoutParams as ViewGroup.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = (250 * applicationContext.resources.displayMetrics.density).toInt()
            playerView.layoutParams = params
            isFullscreen = false
            Handler().postDelayed({
                this@MediaViewer.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }, 2000)
        } else {
            hideSystemUi()
            imgFullOrExit!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.exo_controls_fullscreen_exit
                )
            )
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            if (supportActionBar != null) {
                supportActionBar!!.hide()
            }
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val params: ViewGroup.LayoutParams =
                playerView.layoutParams as ViewGroup.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            playerView.layoutParams = params

            isFullscreen = true
            Handler().postDelayed({
                this@MediaViewer.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }, 2000)
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
        Log.d(TAG, "isloading $isLoading")

    }


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)

        if (playbackState == Player.STATE_READY) {

            Log.d(TAG, "playstate ready $isBuffering $isClickTimeBar")
            progressBar!!.stop()
            layoutBtnControllerPlay!!.animate().alpha(1f)
            if (playWhenReady) {
                showBtnPause()
                playerState = true
            } else {
                playerState = false
                showBtnPlay()
            }


        } else if (playbackState == Player.STATE_BUFFERING) {
            Log.d(TAG, "playstate buffer")
            startPlayer()
            showBtnPlay()
            isBuffering = true
            progressBar!!.start()
            layoutBtnControllerPlay!!.animate().alpha(0f)

        } else if (playbackState == Player.STATE_IDLE) {
            Log.d(TAG, "playstate idle")
            showBtnPlay()
            progressBar!!.start()


        } else if (playbackState == Player.STATE_ENDED) {
            Log.d(TAG, "playstate end")
            showBtnReplay()
            progressBar!!.stop()

        }

        if (playbackState == Player.STATE_READY && !durationSet) {
            durationMedia = player!!.duration
            durationSet = true
        }

    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        super.onPlayerError(error)
        Log.d(TAG, "play error $error")
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        super.onShuffleModeEnabledChanged(shuffleModeEnabled)
    }

    override fun onSeekProcessed() {
        super.onSeekProcessed()
//        progress_bar.visibility = View.VISIBLE
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
//        hideSystemUi()
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
//        hideSystemUi()
//        requestFullScreenIfLandscape()
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
        val orientation = newConfig.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            isFullscreen = false

            Log.d(TAG, "potrait")
            imgFullOrExit!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.exo_controls_fullscreen_enter
                )
            )
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            if (supportActionBar != null) {
                supportActionBar!!.show()
            }
            showSystemUI()
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val params: ViewGroup.LayoutParams = playerView.layoutParams as ViewGroup.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = (250 * applicationContext.resources.displayMetrics.density).toInt()
            playerView.layoutParams = params
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "landscape")
            hideSystemUi()
            isFullscreen = true
            imgFullOrExit!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.exo_controls_fullscreen_exit
                )
            )
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            if (supportActionBar != null) {
                supportActionBar!!.hide()
            }
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val params: ViewGroup.LayoutParams = playerView.layoutParams as ViewGroup.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            playerView.layoutParams = params
        }

    }

    private fun featureDoubleClick() {
        containerController!!.setOnClickListener {

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
                val nextPosition = player?.currentPosition?.plus(10000)
                player!!.seekTo(nextPosition!!)
                txtNextSeek!!.text = "10 Seconds"
                txtNextSeek!!.animate().alpha(1f)
                Handler().postDelayed({
                    txtNextSeek!!.animate().alpha(0.0f)
                }, 1000)
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
                val replayPosition = player?.currentPosition?.minus(10000)
                txtPreviewSeek!!.text = "10 Seconds"
                txtPreviewSeek!!.animate().alpha(1f)
                Handler().postDelayed({
                    txtPreviewSeek!!.animate().alpha(0.0f)
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


    private fun hideSystemUi() {
        val decorView: View = this.getWindow().getDecorView()
        val uiOptions = decorView.systemUiVisibility
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = newUiOptions
    }

    private fun showSystemUI() {
        val decorView: View = this.getWindow().getDecorView()
        val uiOptions = decorView.systemUiVisibility
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE.inv()
        newUiOptions = newUiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
        decorView.systemUiVisibility = newUiOptions
    }

    private fun hideControllerMedia() {
        btnPlay!!.visibility = View.GONE
        btnPause!!.visibility = View.GONE
    }

    private fun pausePlayer() {
        Log.d(TAG, "player pause")
        player!!.playWhenReady = false
        player!!.playbackState;
    }

    private fun startPlayer() {
        player!!.playWhenReady = true
        player!!.playbackState;
    }

    private fun showBtnPlay() {
        btnPlay!!.visibility = View.VISIBLE
        btnPause!!.visibility = View.GONE
        btnReplay!!.visibility = View.GONE
    }

    private fun showBtnPause() {
        btnReplay!!.visibility = View.GONE
        btnPlay!!.visibility = View.GONE
        btnPause!!.visibility = View.VISIBLE
    }

    private fun showBtnReplay() {
        btnPlay!!.visibility = View.GONE
        btnPause!!.visibility = View.GONE
        btnReplay!!.visibility = View.VISIBLE
    }

    private fun detectedNetWork(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    inner class ConnectivityCallback : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            connectivityNetwork = connected
            Log.d(TAG, "connected")


        }

        override fun onLost(network: Network) {
            Log.d(TAG, "lost")
            connectivityNetwork = false

        }
    }
}

