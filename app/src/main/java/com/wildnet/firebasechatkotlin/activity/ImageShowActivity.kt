package com.wildnet.firebasechatkotlin.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.utility.Utility
import kotlinx.android.synthetic.main.activity_image_show.*


class ImageShowActivity : AppCompatActivity(), View.OnClickListener {

    private var checkToolbarVisibleOrNot = false
    private var dialog: Dialog? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_show)
        supportActionBar!!.hide()

        val win = window
        win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setImageAndNameInToolbar()
        iv_image.setOnClickListener(this)
        tv_userChatName.text = intent.getStringExtra("")
        if (Utility.getCurrentUser() == intent.getStringExtra("imageSenderUser")) {
            tv_userChatName.text = "You"
        } else {
            tv_userChatName.text = intent.getStringExtra("name")
        }
        iv_deleteImage.setOnClickListener(this)
        iv_shareImage.setOnClickListener(this)
        iv_backUserChat.setOnClickListener {
            super@ImageShowActivity.onBackPressed()
        }

    }

    private fun setImageAndNameInToolbar() {

        val transformation = object : Transformation {
            override fun transform(source: Bitmap): Bitmap {
                val targetWidth = iv_image.width

                val aspectRatio = source.height.toDouble() / source.width.toDouble()
                val targetHeight = (targetWidth * aspectRatio).toInt()
                val result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
                if (result != source) {
                    // Same bitmap is returned if sizes are the same
                    source.recycle()
                }
                return result
            }

            override fun key(): String {
                return "transformation" + " desiredWidth"
            }
        }
        Picasso.get().load(intent.getStringExtra("imageUrl")).transform(transformation)
                .into(iv_image, object : Callback {
                    override fun onSuccess() {
                        Log.e("Picasso ", "success")
                    }

                    override fun onError(e: Exception) {
                        Log.e("Picasso ", "fail")
                    }
                })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_image -> {
                if (checkToolbarVisibleOrNot) {
                    checkToolbarVisibleOrNot = false
                    frameLayout.visibility = View.VISIBLE
                } else {
                    checkToolbarVisibleOrNot = true
                    frameLayout.visibility = View.GONE
                }
            }
            R.id.iv_deleteImage -> {
                AlertDialog.Builder(this)
                        .setTitle("Delete Image")
                        .setMessage("Are you sure to delete")
                        .setPositiveButton("Yes") { dialogInterface, i ->
                            deleteImage()
                        }
                        .setNegativeButton("No") { dialogInterface, i ->
                            Toast.makeText(this, "good", Toast.LENGTH_SHORT).show()
                        }
                        .show()
            }
            R.id.iv_shareImage -> {
                dialog = Dialog(this)
                //dialog!!.setTitle("Share Image")
                dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog!!.setContentView(R.layout.dailog_image_share)
                dialog!!.show()
                dialog!!.findViewById<TextView>(R.id.tv_user).setOnClickListener {
                  sendToUser()
                }

                dialog!!.findViewById<TextView>(R.id.tv_group).setOnClickListener {
                    sendToGroup()
                }
            }
        }
    }

    private fun sendToGroup() {
        val intent1=Intent(this,ShowAllUserOrGroupActivity::class.java)
        intent1.putExtra("group","Group")
        intent1.putExtra("imageUrl",intent.getStringExtra("imageUrl"))
        startActivity(intent1)
        dialog!!.dismiss()
    }

    private fun sendToUser() {
        val intent1=Intent(this,ShowAllUserOrGroupActivity::class.java)
        intent1.putExtra("user","User")
        intent1.putExtra("imageUrl",intent.getStringExtra("imageUrl"))
        startActivity(intent1)
        dialog!!.dismiss()
    }

    private fun deleteImage() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val userEmail1 = Utility.getCurrentUser()!!.replace(".", "")
        val email1 = intent.getStringExtra("email").replace(".", "")
        usersdRef.child(userEmail1).child("chatList").child(userEmail1 + "2" + email1)
                .child("chat").child(intent.getStringExtra("key")).removeValue().addOnSuccessListener {
                    Log.e("deleteMsg", "success")
                    finish()
                }.addOnFailureListener {
                    Log.e("deleteMsg", "fail")
                    Toast.makeText(this, "not delete", Toast.LENGTH_SHORT).show()
                }
    }



}
