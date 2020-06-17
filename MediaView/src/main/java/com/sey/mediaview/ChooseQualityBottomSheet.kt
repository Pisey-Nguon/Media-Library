package com.sey.mediaview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_main.*

class ChooseQualityBottomSheet:BottomSheetDialogFragment() {
    companion object{
        private val shareOptionDialog: ChooseQualityBottomSheet? = null
        fun getInstance(): ChooseQualityBottomSheet{
            return shareOptionDialog ?: ChooseQualityBottomSheet()
        }
    }
    val TAG = "ShareOptionDialog"
    private lateinit var mCallback: callBackChooseQuality
    private lateinit var lv1080:LinearLayout
    private lateinit var lv720:LinearLayout
    private lateinit var lv480:LinearLayout
    private lateinit var lv360:LinearLayout
    private lateinit var lvAuto:LinearLayout

    private lateinit var cAuto:ImageView
    private lateinit var c1080:ImageView
    private lateinit var c720:ImageView
    private lateinit var c480:ImageView
    private lateinit var c360:ImageView

    private var indexVisibleCheck=0

    fun registerCallback(callback: callBackChooseQuality){
        this.mCallback = callback
    }
    fun setValueForCheckVisible(index: Int){
        this.indexVisibleCheck=index
    }
    fun visibleToCheck(index:Int){
        when (index) {
            0 -> {
                cAuto.visibility=View.VISIBLE
            }
            1 -> {
                c1080.visibility=View.VISIBLE
            }
            2 -> {
                c720.visibility=View.VISIBLE
            }
            3 -> {
                c480.visibility=View.VISIBLE
            }
            4 -> {
                c360.visibility=View.VISIBLE
            }
        }
    }
    fun setHideLayout(index: Int){
        when (index) {
            1 -> {
                lv1080.visibility=View.GONE
            }
            2 -> {
                lv720.visibility=View.GONE
            }
            3 -> {
                lv480.visibility=View.GONE
            }
            4 -> {
                lv360.visibility=View.GONE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_share_option_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewComponent(view)
        componentFunction()
        visibleToCheck(indexVisibleCheck)
    }

    private fun initViewComponent(view: View) {
        lvAuto=view.findViewById(R.id.lv_auto)
        lv1080=view.findViewById(R.id.lv_1080p)
        lv720=view.findViewById(R.id.lv_720p)
        lv480=view.findViewById(R.id.lv_480p)
        lv360=view.findViewById(R.id.lv_360p)
        cAuto=view.findViewById(R.id.check_auto)
        c1080=view.findViewById(R.id.check_1080)
        c720=view.findViewById(R.id.check_720)
        c480=view.findViewById(R.id.check_480)
        c360=view.findViewById(R.id.check_360)
    }

    private fun componentFunction() {
        lvAuto.setOnClickListener{
            mCallback.callAuto()
            dismiss()
        }
        lv1080.setOnClickListener {
            mCallback.call1080()
            dismiss()
        }
        lv720.setOnClickListener {
            mCallback.call720()
            dismiss()
        }
        lv480.setOnClickListener {
            mCallback.call480()
            dismiss()
        }
        lv360.setOnClickListener {
            mCallback.call360()
            dismiss()
        }
    }

    interface callBackChooseQuality{
        fun callAuto()
        fun call1080()
        fun call720()
        fun call480()
        fun call360()
    }
}