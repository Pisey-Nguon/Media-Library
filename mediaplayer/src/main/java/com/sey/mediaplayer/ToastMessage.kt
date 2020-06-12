package com.sey.mediaplayer

import android.content.Context
import android.widget.Toast

class ToastMessage(value:String) {
    fun toast(context: Context,text:String){
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show()
    }
}