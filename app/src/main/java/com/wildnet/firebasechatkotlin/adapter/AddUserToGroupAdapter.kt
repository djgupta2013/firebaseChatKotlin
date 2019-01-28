package com.wildnet.firebasechatkotlin.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.model.GroupUserModel
import com.wildnet.firebasechatkotlin.model.ShowAllGroupMemberModel
import com.wildnet.firebasechatkotlin.model.UserModel
import kotlinx.android.synthetic.main.user_adapter_layout.view.*
import java.util.ArrayList

class AddUserToGroupAdapter(userModelList: List<UserModel>, allGroupUserList1: List<GroupUserModel>,
                            context: Context,
                            groupName1: String) : RecyclerView.Adapter<AddUserToGroupAdapter.UserViewHolder>() {

    private var allUserList = userModelList
    private val mContext = context
    private val groupName=groupName1
     var allGroupUserList = allGroupUserList1
    private var userCheck = true
    private var progressBar: ProgressDialog? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): UserViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.user_adapter_layout, viewGroup, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return allUserList.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: UserViewHolder, p1: Int) {
        allGroupUserList.size
        for (item in allGroupUserList) {
            if (item.email == allUserList[p1].email) {
                userCheck = false
                break
            } else {
                userCheck = true
            }
        }
        if (userCheck) {
            holder.userName.text = allUserList[p1].name
            holder.userEmail.text = allUserList[p1].email
        } else {
            holder.rlAddUser.setBackgroundColor(Color.TRANSPARENT)
            holder.userName.text = allUserList[p1].name
            holder.userEmail.text = mContext.getString(R.string.already_added)
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var checkBox = itemView.chb_user!!
        var userName = itemView.tv_userName!!
        var userEmail = itemView.tv_userEmail!!
        val rlAddUser= itemView.rl_addUser!!

        init {
            checkBox.setOnClickListener(this)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition

            allGroupUserList.size
            for (item in allGroupUserList) {
                if (item.email == allUserList[position].email) {
                    userCheck = false
                    break
                } else {
                    userCheck = true
                }
            }
            if (userCheck) {
                AlertDialog.Builder(mContext)
                        .setTitle("Add user")
                        .setMessage("Are you sure to add " + allUserList[position].name)
                        .setPositiveButton("Yes") { dialogInterface, i ->
                            addUserToGroup(position)
                        }
                        .setNegativeButton("No") { dialogInterface, i ->
                            //Toast.makeText(mContext, "good", Toast.LENGTH_LONG).show()
                        }
                        .show()
            }else{
                Toast.makeText(mContext, "user already added", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUserToGroup(position: Int) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        progressBar = ProgressDialog(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressBar!!.setMessage("Fetching all user......")
        progressBar!!.setCancelable(false)
        val groupUserModel=GroupUserModel(allUserList[position].email,allUserList[position].name,
                "no","yes")
        ((allGroupUserList as ArrayList<GroupUserModel>).add(groupUserModel))

        usersdRef.child(groupName).child("user").setValue(allGroupUserList)
                .addOnSuccessListener {
                    Log.e("addUserToGroup","success")
                }.addOnFailureListener {
                    Log.e("addUserToGroup","fail")
                }
    }
}