@file:Suppress("DEPRECATION")

package com.wildnet.firebasechatkotlin.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.adapter.ShowAllGroupMemberAdapter
import com.wildnet.firebasechatkotlin.model.ShowAllGroupMemberModel
import com.wildnet.firebasechatkotlin.utility.Utility.Companion.getCurrentUser
import kotlinx.android.synthetic.main.activity_show_all_group_member.*
import java.util.*

@Suppress("DEPRECATION")
class ShowAllGroupMemberActivity : AppCompatActivity(), View.OnClickListener {

    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private var progressBar: ProgressDialog? = null
    internal lateinit var userList: List<ShowAllGroupMemberModel>
    var checkUserAdmin : String ="no"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_all_group_member)
        supportActionBar!!.hide()
        imageViewBack.setOnClickListener(this)
        iv_addUser.setOnClickListener(this)
        recyclerViewLayoutManager = LinearLayoutManager(applicationContext)
        recyclerViewAllGroupMember.layoutManager = recyclerViewLayoutManager
        getAllGroupUser()
    }

    private fun getAllGroupUser() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        progressBar = ProgressDialog(this, android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressBar!!.setMessage("Fetching all user......")
        progressBar!!.setCancelable(true)
        progressBar!!.show()
        usersdRef.child(intent.getStringExtra("groupName")).child("user")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        userList = ArrayList()
                        var check=false
                        for (ds in dataSnapshot.children) {
                            val name = ds.child("name").getValue(String::class.java)
                            val email = ds.child("email").getValue(String::class.java)
                            val admin = ds.child("adminTxt").getValue(String::class.java)
                            val showAllGroupMemberModel = ShowAllGroupMemberModel(email, name, admin)
                            (userList as ArrayList<ShowAllGroupMemberModel>).add(showAllGroupMemberModel)
                            if(getCurrentUser()==email && admin =="admin"){
                                check=true
                                iv_addUser.visibility=View.VISIBLE
                                checkUserAdmin=admin
                            }else{
                                if(!check) {
                                    iv_addUser.visibility = View.GONE
                                    checkUserAdmin=""
                                }
                            }
                        }
                        setDataToAdapter()
                    }
                    override fun onCancelled(p0: DatabaseError) {
                        progressBar!!.dismiss()
                    }
                })
    }

    private fun setDataToAdapter() {
        val showAllGroupMemberAdapter = ShowAllGroupMemberAdapter(this, userList,checkUserAdmin,getIntent().getStringExtra("groupName"))
        recyclerViewAllGroupMember.adapter = showAllGroupMemberAdapter
        progressBar!!.dismiss()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.imageViewBack -> {
                super@ShowAllGroupMemberActivity.onBackPressed()
            }
            R.id.iv_addUser -> {
               val intent =Intent(this,AddUserToGroupActivity::class.java)
                intent.putExtra("groupName",getIntent().getStringExtra("groupName"))
                startActivity(intent)
            }
        }
    }
}