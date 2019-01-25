package com.wildnet.firebasechatkotlin.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.crashlytics.android.Crashlytics
import com.wildnet.firebasechatkotlin.R
import io.fabric.sdk.android.Fabric

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val win = window
        win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        // Transparent Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_splash_screen)
        supportActionBar!!.hide()
        Fabric.with(this, Crashlytics())
        supportActionBar!!.hide()

        val background = object : Thread() {
            override fun run() {
                try {
                    // Thread will sleep for 2 seconds
                    Thread.sleep((2 * 1000).toLong())
                    //val i = Intent(baseContext, MobileManufaturerCheckActivity::class.java)
                    val i = Intent(baseContext, LoginActivity::class.java)
                    startActivity(i)
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        // start thread
        background.start()
    }
}
