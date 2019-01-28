package com.wildnet.firebasechatkotlin.activity

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.fragment.MyPagerAdapter
import com.wildnet.firebasechatkotlin.service.NetworkReceiverService
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    internal var auth: FirebaseAuth? = null
    private var userEmail: String = ""
    private val titles = arrayOf<CharSequence>("User", "GroupChat")
    private val numberoftabs = 2
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       // FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth!!.currentUser
        if (currentUser != null) {
            userEmail = currentUser.email!!
        }

        val myPagerAdapter = MyPagerAdapter(supportFragmentManager, titles, numberoftabs)
        pager.adapter = myPagerAdapter
        // val tabLayout = findViewById<View>(R.id.tablayout) as TabLayout   mHSVSlidingTabLayout
        mHSVSlidingTabLayout.setDistributeEvenly(true)
        mHSVSlidingTabLayout.setCustomTabColorizer { resources.getColor(R.color.tabsScrollColor) }
        mHSVSlidingTabLayout.setOnPageChangeListener(
                object : ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        //hideKeyboard()
                    }
                    override fun onPageSelected(position: Int) {}

                    override fun onPageScrollStateChanged(state: Int) {}
                })
        mHSVSlidingTabLayout.setViewPager(pager)
        //scheduleJob()
        setUserActiveOrInactive()
        val list = listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CALENDAR)
        permission(this)
    }

    private fun permission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Dexter.withActivity(context as Activity)
                    .withPermissions(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA)
                    .withListener(
                            object : MultiplePermissionsListener {
                                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                    // checkForCallLogPermission ( );
                                    //displayLocationSettingsRequest(context)
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                        permissions: List<PermissionRequest>, token: PermissionToken) {
                                    showPermissionRationale(token, context)
                                }
                            })
                    .check()
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun showPermissionRationale(token: PermissionToken, context: Context) {
        AlertDialog.Builder(context)
                .setTitle("We need this permission")
                .setMessage(R.string.permission_message)
                .setNegativeButton(
                        android.R.string.cancel
                ) { dialog, which ->
                    dialog.dismiss()
                    token.cancelPermissionRequest()
                }
                .setPositiveButton(
                        android.R.string.ok
                ) { dialog, which ->
                    dialog.dismiss()
                    token.continuePermissionRequest()
                }
                .setOnDismissListener { token.cancelPermissionRequest() }
                .show()
    }

    private fun setUserActiveOrInactive() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        userEmail = userEmail.replace(".", "")
        usersdRef.child(userEmail).child("activeUser").setValue("online")
        usersdRef.child(userEmail).child("activeUser").onDisconnect().setValue(ServerValue.TIMESTAMP)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scheduleJob() {
        val jobInfo: JobInfo = JobInfo.Builder(0, ComponentName(this, NetworkReceiverService::class.java))
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build()
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }

    override fun onClick(v: View?) {
        FirebaseAuth.getInstance().signOut()
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId
            if (id == R.id.action_logout) {
                showLogoutDialog()
            } else if (id == R.id.action_groupCreate) {
                val i = Intent(this, CreateGroupActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
            }
        return super.onOptionsItemSelected(item)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure to logout")
                .setPositiveButton("Yes") { dialogInterface, i ->
                    logoutUser()
                }
                .setNegativeButton("No") { dialogInterface, i ->
                    Toast.makeText(applicationContext, "abc", Toast.LENGTH_LONG).show()
                }
                .show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}