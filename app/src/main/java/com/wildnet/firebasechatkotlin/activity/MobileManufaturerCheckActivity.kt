package com.wildnet.firebasechatkotlin.activity

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.wildnet.firebasechatkotlin.R

class MobileManufaturerCheckActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_manufaturer)

        val intent = Intent()
        val manufacturer = android.os.Build.MANUFACTURER
        Toast.makeText(this, "$manufacturer", Toast.LENGTH_LONG).show()

        when (manufacturer) {
            "xiaomi" ->
                intent.component =
                        ComponentName("com.miui.securitycenter",
                                "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
            "oppo" ->
                intent.component =
                        ComponentName("com.coloros.safecenter",
                                "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            "vivo" ->
                intent.component =
                        ComponentName("com.vivo.permissionmanager",
                                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
        }

        val list = this.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (list.size > 0) {
            startActivity(intent)
        }
    }
}
