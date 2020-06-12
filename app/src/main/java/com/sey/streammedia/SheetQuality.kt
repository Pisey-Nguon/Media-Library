package com.sey.streammedia

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.arthurivanets.bottomsheets.BaseBottomSheet
import com.arthurivanets.bottomsheets.config.BaseConfig
import com.arthurivanets.bottomsheets.config.Config

class SheetQuality @JvmOverloads constructor(
    hostActivity: Activity,
    config: BaseConfig = Config.Builder(
        hostActivity
    ).build()
) :
    BaseBottomSheet(hostActivity, config) {
    public override fun onCreateSheetContentView(context: Context): View {
        return LayoutInflater.from(context).inflate(
            R.layout.layout_choose_resolution,
            this,
            false
        )
    }
}