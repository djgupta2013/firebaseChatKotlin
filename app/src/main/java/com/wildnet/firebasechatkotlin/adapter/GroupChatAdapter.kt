package com.wildnet.firebasechatkotlin.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.activity.ImageShowActivity
import com.wildnet.firebasechatkotlin.model.GroupChatModel
import com.wildnet.firebasechatkotlin.utility.Utility.Companion.getCurrentUser
import kotlinx.android.synthetic.main.group_chat_layout.view.*

class GroupChatAdapter(mContext: Context, chatList1: List<GroupChatModel>, groupName1: String) :
        RecyclerView.Adapter<GroupChatAdapter.UserViewHolder>() {
    private var chatList: List<GroupChatModel> = chatList1
    val groupName = groupName1
    val context = mContext

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): UserViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.group_chat_layout, viewGroup, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        Log.e("size", chatList.size.toString())
        return chatList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        if (getCurrentUser().equals(chatList[position].userEmail)) {
            if (chatList[position].imageUrl == "") {
                holder.groupRightChatView.visibility = View.VISIBLE
                holder.groupLeftChatView.visibility = View.GONE
                holder.rlImageLeftSet.visibility = View.GONE
                holder.rlImageRightSet.visibility = View.GONE

                holder.tvUserNameRight.text = chatList[position].userName
                holder.tvGroupChatMsgSetRight.text = chatList[position].chatMsg
                holder.tvGroupChatTimeSetRight.text = chatList[position].currTime
            } else {
                holder.rlImageRightSet.visibility = View.VISIBLE
                holder.groupRightChatView.visibility = View.GONE
                holder.groupLeftChatView.visibility = View.GONE
                holder.rlImageLeftSet.visibility = View.GONE

                holder.setRightImageTime.text = chatList[position].currTime
                Picasso.get().load(chatList[position].imageUrl).resize(140, 140)
                        .centerCrop().into(holder.setRightChatImage, object : Callback {
                            override fun onSuccess() {
                                Log.e("Picasso ", "success")
                            }

                            override fun onError(e: Exception) {
                                Log.e("Picasso ", "fail")
                            }
                        })
            }

        } else {
            if (chatList[position].imageUrl == "") {
                holder.groupLeftChatView.visibility = View.VISIBLE
                holder.groupRightChatView.visibility = View.GONE
                holder.rlImageLeftSet.visibility = View.GONE
                holder.rlImageRightSet.visibility = View.GONE

                holder.tvUserNameLeft.text = chatList[position].userName
                holder.tvGroupChatMsgSetLeft.text = chatList[position].chatMsg
                holder.tvGroupChatTimeSetLeft.text = chatList[position].currTime
            } else {
                holder.rlImageLeftSet.visibility = View.VISIBLE
                holder.groupRightChatView.visibility = View.GONE
                holder.rlImageRightSet.visibility = View.GONE
                holder.groupLeftChatView.visibility = View.GONE

                holder.setLeftImageTime.text = chatList[position].currTime
                Picasso.get().load(chatList[position].imageUrl).resize(150, 150)
                        .centerCrop().into(holder.setLeftChatImage, object : Callback {
                            override fun onSuccess() {
                                holder.setLeftUserName.visibility = View.VISIBLE
                                holder.setLeftUserName.text = chatList[position].userName
                                Log.e("Picasso ", "success")
                            }

                            override fun onError(e: Exception) {
                                Log.e("Picasso ", "fail")
                            }
                        })
            }
        }
    }


    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
            View.OnLongClickListener {
        //Right chat
        var groupRightChatView = itemView.groupRightChatView!!
        var tvUserNameRight = itemView.tv_userNameRight!!
        var tvGroupChatMsgSetRight = itemView.tv_groupChatMsgSetRight!!
        var tvGroupChatTimeSetRight = itemView.tv_groupChatTimeSetRight!!
        var rlImageRightSet = itemView.rl_imageRightSet!!
        var setRightChatImage = itemView.setRightChatImage!!
        //var setRightUserName= itemView.setRightUserName!!
        var setRightImageTime = itemView.setRightImageTime!!
        //Left chat
        var groupLeftChatView = itemView.groupLeftChatView!!
        var tvUserNameLeft = itemView.tv_userNameLeft!!
        var tvGroupChatMsgSetLeft = itemView.tv_groupChatMsgSetLeft!!
        var tvGroupChatTimeSetLeft = itemView.tv_groupChatTimeSetLeft!!
        var rlImageLeftSet = itemView.rl_imageLeftSet!!
        var setLeftChatImage = itemView.setLeftChatImage!!
        var setLeftUserName = itemView.setLeftUserName!!
        var setLeftImageTime = itemView.setLeftImageTime!!

        init {
            groupRightChatView.setOnLongClickListener(this)
            groupLeftChatView.setOnLongClickListener(this)
            setRightChatImage.setOnLongClickListener(this)
            rlImageLeftSet.setOnLongClickListener(this)
            setRightChatImage.setOnClickListener(this)
            rlImageLeftSet.setOnClickListener(this)
            //itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            val key = chatList[position].key
            val intent = Intent(context, ImageShowActivity::class.java)
            intent.putExtra( "key",key)
            intent.putExtra("imageSenderUser",chatList[position].userEmail)
            intent.putExtra("imageUrl", chatList[position].imageUrl)
            context.startActivity(intent)
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            val key = chatList[position].key
            val image = chatList[position].imageUrl
            val msgTitle: String
            msgTitle = if (image != "") {
                "Delete Image"
            } else {
                "Delete Message"
            }
            AlertDialog.Builder(context)
                    //set icon
                    //.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(msgTitle)
                    .setMessage("Are you sure to delete")
                    .setPositiveButton("Yes") { dialogInterface, i ->
                        deleteMsg(key!!)
                    }
                    .setNegativeButton("No") { dialogInterface, i ->
                        Toast.makeText(context, "good", Toast.LENGTH_LONG).show()
                    }
                    .show()
            return true
        }

        private fun deleteMsg(key: String) {
            val rootRef = FirebaseDatabase.getInstance().reference
            val usersdRef = rootRef.child("Group")
            usersdRef.child(groupName).child("chatList").child(key).removeValue()
        }

    }
}