package com.sey.streammedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sey.mediaview.MediaViewer
import kotlinx.android.synthetic.main.activity_first.*

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        click_me.setOnClickListener {
            val mediaViewer=MediaViewer()
            mediaViewer.setUrl(getString(R.string.link_media))
            val intent=Intent(this,mediaViewer::class.java)
            startActivity(intent)
        }
    }
}