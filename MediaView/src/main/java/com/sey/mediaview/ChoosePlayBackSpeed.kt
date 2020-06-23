package com.sey.mediaview

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_main.*

class ChoosePlayBackSpeed:BottomSheetDialogFragment() {
    companion object{
        private val shareOptionDialog: ChoosePlayBackSpeed? = null
        fun getInstance(): ChoosePlayBackSpeed{
            return shareOptionDialog ?: ChoosePlayBackSpeed()
        }
    }
    val TAG = "ShareOptionDialog"
    private var mainLayout:View?=null
    private lateinit var mCallback: callBackChoosePlayBackSpeed
    private lateinit var lv0:LinearLayout
    private lateinit var lv1:LinearLayout
    private lateinit var lv2:LinearLayout
    private lateinit var lv3:LinearLayout
    private lateinit var lv4:LinearLayout
    private lateinit var lv5:LinearLayout
    private lateinit var lv6:LinearLayout
    private lateinit var lv7:LinearLayout

    private lateinit var im0:ImageView
    private lateinit var im1:ImageView
    private lateinit var im2:ImageView
    private lateinit var im3:ImageView
    private lateinit var im4:ImageView
    private lateinit var im5:ImageView
    private lateinit var im6:ImageView
    private lateinit var im7:ImageView

    private var indexVisibleCheck=0

    fun registerCallback(callback: callBackChoosePlayBackSpeed){
        this.mCallback = callback
    }
    fun setValueForCheckVisible(index: Int){
        this.indexVisibleCheck=index
    }
    private fun visibleToCheck(index:Int){
        when (index) {
            0 -> {
                im0.visibility=View.VISIBLE
            }
            1 -> {
                im0.visibility=View.VISIBLE
            }
            2 -> {
                im2.visibility=View.VISIBLE
            }
            3 -> {
                im3.visibility=View.VISIBLE
            }
            4 -> {
                im4.visibility=View.VISIBLE
            }
            5 -> {
                im5.visibility=View.VISIBLE
            }
            6 -> {
                im6.visibility=View.VISIBLE
            }
            7 -> {
                im7.visibility=View.VISIBLE
            }
        }
    }
    fun setHideLayout(index: Int){
        when (index) {
            0 -> {
                lv0.visibility=View.GONE
            }
            1 -> {
                lv1.visibility=View.GONE
            }
            2 -> {
                lv2.visibility=View.GONE
            }
            3 -> {
                lv3.visibility=View.GONE
            }
            4 -> {
                lv4.visibility=View.GONE
            }
            5 -> {
                lv5.visibility=View.GONE
            }
            6 -> {
                lv6.visibility=View.GONE
            }
            7 -> {
                lv7.visibility=View.GONE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val bottomSheetBehavior = BottomSheetBehavior.from(mainLayout!!.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainLayout=inflater.inflate(R.layout.layout_choose_speed, container, false)
        return mainLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewComponent(view)
        componentFunction()
        visibleToCheck(indexVisibleCheck)
    }

    private fun initViewComponent(view: View) {
        lv0=view.findViewById(R.id.btn_0_25x)
        lv1=view.findViewById(R.id.btn_0_5x)
        lv2=view.findViewById(R.id.btn_0_75x)
        lv3=view.findViewById(R.id.btn_normal)
        lv4=view.findViewById(R.id.btn_1_25x)
        lv5=view.findViewById(R.id.btn_1_5x)
        lv6=view.findViewById(R.id.btn_1_75x)
        lv7=view.findViewById(R.id.btn_2x)

        im0=view.findViewById(R.id.check_0_25x)
        im1=view.findViewById(R.id.check_0_5x)
        im2=view.findViewById(R.id.check_0_75x)
        im3=view.findViewById(R.id.check_normal)
        im4=view.findViewById(R.id.check_1_25x)
        im5=view.findViewById(R.id.check_1_5x)
        im6=view.findViewById(R.id.check_1_75x)
        im7=view.findViewById(R.id.check_2x)
    }

    private fun componentFunction() {
        lv0.setOnClickListener{
            mCallback.speed0()
            dismiss()
        }
        lv1.setOnClickListener {
            mCallback.speed1()
            dismiss()
        }
        lv2.setOnClickListener{
            mCallback.speed2()
            dismiss()
        }
        lv3.setOnClickListener {
            mCallback.speed3()
            dismiss()
        }
        lv4.setOnClickListener{
            mCallback.speed4()
            dismiss()
        }
        lv5.setOnClickListener {
            mCallback.speed5()
            dismiss()
        }
        lv6.setOnClickListener{
            mCallback.speed6()
            dismiss()
        }
        lv7.setOnClickListener {
            mCallback.speed7()
            dismiss()
        }

    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        mCallback.onDismiss()
    }
    interface callBackChoosePlayBackSpeed{
        fun speed0()
        fun speed1()
        fun speed2()
        fun speed3()
        fun speed4()
        fun speed5()
        fun speed6()
        fun speed7()
        fun onDismiss()
    }
}