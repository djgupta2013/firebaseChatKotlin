package com.wildnet.firebasechatkotlin.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.adapter.ShowImageForAllUserAdapter
import com.wildnet.firebasechatkotlin.adapter.ShowImageForAllGroupAdapter
import com.wildnet.firebasechatkotlin.model.GroupChatModel
import com.wildnet.firebasechatkotlin.model.GroupNameModel
import com.wildnet.firebasechatkotlin.model.UserChatModel
import com.wildnet.firebasechatkotlin.model.UserDetailModel
import com.wildnet.firebasechatkotlin.utility.Utility
import com.wildnet.firebasechatkotlin.utility.Utility.Companion.getCurrentUser
import com.wildnet.firebasechatkotlin.utility.Utility.Companion.getUserName
import kotlinx.android.synthetic.main.activity_show_all_user_or_group.*
import java.util.*

class ShowAllUserOrGroupActivity : AppCompatActivity() {

    private var progressBar: ProgressDialog? = null
    private lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager
    internal lateinit var userList: List<UserDetailModel>
    internal lateinit var groupList: List<GroupNameModel>
    lateinit var userChatModel: UserChatModel
    lateinit var groupChatModel : GroupChatModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_all_user_or_group)
        supportActionBar!!.hide()
        recyclerViewLayoutManager = LinearLayoutManager(this)
        recyclerViewUserList.layoutManager = recyclerViewLayoutManager
        if (intent.getStringExtra("user") == "User") {
            getAllUser()
            userChatModel = UserChatModel("", Utility.getCurrTime(),
                    Utility.getCurrentUser(), "", intent.getStringExtra("imageUrl"))
            // userChatModel.currentTime=Utility.getCurrTime()
        } else if (intent.getStringExtra("group") == "Group") {
            groupChatModel = GroupChatModel(getCurrentUser(),
                    getUserName(this@ShowAllUserOrGroupActivity), "",
                    "", "", intent.getStringExtra("imageUrl"))
            tv_groupName1.text="All Group"
            getAllGroup()
        }

        imageViewBack.setOnClickListener {
            super@ShowAllUserOrGroupActivity.onBackPressed()
        }
    }

    private fun getAllGroup() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        progressBar = ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressBar!!.setMessage("Fetching all group......")
        progressBar!!.setCancelable(false)
        progressBar!!.show()

        usersdRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot : DataSnapshot) {

                var groupNameModel: GroupNameModel
                groupList = ArrayList()
                for (ds in dataSnapshot.children) {
                    val groupName = ds.key
                    if (ds.child("user").value.toString().contains(Utility.getCurrentUser()!!)) {
                        groupNameModel = GroupNameModel(groupName)
                        (groupList as ArrayList<GroupNameModel>).add(groupNameModel)
                    }
                }
                Log.e("all Group", groupList.toString())



                setDataToAdapter("group")

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun getAllUser() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        progressBar = ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressBar!!.setMessage("Fetching all user......")
        progressBar!!.setCancelable(false)
        progressBar!!.show()

        usersdRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var userDetailModel: UserDetailModel
                userList = ArrayList()
                for (ds in dataSnapshot.children) {
                    val emailcheck: String = ds.child("email").getValue(String::class.java)!!
                    if (emailcheck == Utility.getCurrentUser()) {
                        Log.e("user email", "" + emailcheck)
                        val name: String = ds.child("name").getValue(String::class.java)!!
                        Utility.saveCurrentUserName(applicationContext, "user_name", name)
                    } else {
                        Log.e("user email", "" + emailcheck)
                        val name: String = ds.child("name").getValue(String::class.java)!!
                        userDetailModel = UserDetailModel(emailcheck, name)
                        (userList as ArrayList<UserDetailModel>).add(userDetailModel)
                    }
                }
                setDataToAdapter("user")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressBar!!.dismiss()
                Log.d("onCancelled", "onCancelled")
            }
        })
    }

    private fun setDataToAdapter(check : String) {
        if(check =="user") {
            val showAllUserOrGroupAdapter = ShowImageForAllUserAdapter(this, userList, userChatModel)
            // val userListAdapter = UserListAdapter(context, userList)
            if (recyclerViewUserList != null)
                recyclerViewUserList.adapter = showAllUserOrGroupAdapter
        }else if(check == "group"){
            val showImageForAllUserAdapter=ShowImageForAllGroupAdapter(groupList,this,groupChatModel)
            if (recyclerViewUserList != null)
                recyclerViewUserList.adapter=showImageForAllUserAdapter
        }
        progressBar!!.dismiss()
    }
}
