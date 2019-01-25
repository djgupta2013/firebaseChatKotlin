package com.wildnet.firebasechatkotlin.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.activity.UserChatActivity
import com.wildnet.firebasechatkotlin.model.UserChatModel
import com.wildnet.firebasechatkotlin.model.UserDetailModel
import com.wildnet.firebasechatkotlin.utility.Utility
import java.util.*

class ShowImageForAllUserAdapter(context: Context?, userList1: List<UserDetailModel>, userChatModel1: UserChatModel)
    : RecyclerView.Adapter<ShowImageForAllUserAdapter.UserViewHolder>() {

    var userList: List<UserDetailModel> = userList1
    val userChatModel=userChatModel1
    private var mContext = context

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): UserViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.user_adapter_layout, viewGroup, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, p1: Int) {
        holder.userName.text = userList[p1].name
        holder.userEmail.text = userList[p1].email
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var userName: TextView = itemView.findViewById(R.id.tv_userName)
        var userEmail: TextView = itemView.findViewById(R.id.tv_userEmail)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            val rootRef = FirebaseDatabase.getInstance().reference
            val usersdRef = rootRef.child("User")
            val email1 = userList[position].email.replace(".", "")
            val userEmail1 = Utility.getCurrentUser()!!.replace(".", "")
            userChatModel.currentTime=Utility.getCurrTime()
            usersdRef.child(userEmail1).child("chatList").child(userEmail1 + "2" + email1)
                    .child("chat").child(Date().time.toString()).setValue(userChatModel)

            usersdRef.child(email1).child("chatList").child(email1 + "2" + userEmail1)
                    .child("chat").child(Date().time.toString()).setValue(userChatModel)

            Log.e("position", "" + position)
            val name: String = userList[position].name
            val email: String = userList[position].email
            val i = Intent(mContext, UserChatActivity::class.java)
            i.putExtra("email", email)
            i.putExtra("name", name)
            i.putExtra("imageSend","yes")
            mContext!!.startActivity(i)
        }
    }
}