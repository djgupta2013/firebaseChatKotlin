package com.wildnet.firebasechatkotlin.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class Utility {

    companion object {
        fun isValidEmail(target: CharSequence?): Boolean {
            return if (target == null) {
                false
            } else {
                android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
            }
        }

        fun isOnline(context: Context): Boolean {
            var netInfo: NetworkInfo? = null
            netInfo = try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                cm.activeNetworkInfo
            } catch (e: NullPointerException) {
                null
            }

            return netInfo != null && netInfo.isConnectedOrConnecting
        }

        fun getCurrTime(): String {
            val df = SimpleDateFormat(" hh:mm aa")
            return df.format(Calendar.getInstance().time)
        }

        fun getCurrentUser(): String? {
            var userEmail : String? =null
           val auth = FirebaseAuth.getInstance()
            val currentUser = auth!!.currentUser
            if (currentUser != null) {
                userEmail = currentUser.email!!
                Log.e("token", currentUser.uid)
            }
            return userEmail
        }

        fun saveCurrentUserName(mContext: Context?, constant: String, name: String) {
            if (mContext != null) {
                val preference = mContext.getSharedPreferences("Preferences_", Context.MODE_PRIVATE).edit()
                preference.putString(constant, name)
                preference.apply()
            }
        }

        fun getUserName(mContext: Context?): String {
            if (mContext != null) {
                val preference = mContext.getSharedPreferences("Preferences_", Context.MODE_PRIVATE)
                return preference.getString("user_name", "")
            }
            return ""
        }

        fun getBitmapFromURL(src: String): Bitmap? {
            return try {
                Log.e("src", src)
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                val myBitmap = BitmapFactory.decodeStream(input)
                Log.e("Bitmap", "returned")
                myBitmap
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Exception", e.message)
                null
            }

        }
    }
}