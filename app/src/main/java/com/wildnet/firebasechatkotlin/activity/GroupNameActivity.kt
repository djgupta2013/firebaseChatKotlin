package com.wildnet.firebasechatkotlin.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.model.GroupUserModel
import kotlinx.android.synthetic.main.activity_group_name.*
import java.util.*

class GroupNameActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var userList: List<GroupUserModel>
    private var progressBar: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_name)
        supportActionBar!!.hide()
        val intent = intent
        var bundle=Bundle()
        bundle=intent.extras
        btn_createGroup.setOnClickListener(this)
        iv_backGroupName.setOnClickListener {
            super@GroupNameActivity.onBackPressed()
        }
       userList= (bundle.getSerializable("userList") as List<GroupUserModel>?)!!
    }

    override fun onClick(v: View?) {
         val groupName : String=et_groupName.text.toString().trim()
        progressBar = ProgressDialog(this@GroupNameActivity, android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressBar!!.setMessage("Please wait......")
        progressBar!!.setCancelable(false)
        if(groupName != ""){
            progressBar!!.show()
            setDataInFirebase(groupName)
        }else{
            progressBar!!.dismiss()
            Toast.makeText(this, "Group name require", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setDataInFirebase(groupName: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        val random = Random()
        val number=random.nextInt(50) + 10
        usersdRef.child(groupName+""+number).child("user").setValue(userList).addOnSuccessListener {
            progressBar!!.dismiss()
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }.addOnFailureListener{
            progressBar!!.dismiss()
            Log.e("fail","GroupNameActivity")
        }
    }
}
