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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.activity.ImageShowActivity
import com.wildnet.firebasechatkotlin.model.UserChatModel
import kotlinx.android.synthetic.main.layout_chat_view.view.*

class UserChatAdapter(mContext1: Context, chatList1: List<UserChatModel>, email1: String, name1: String) :
        RecyclerView.Adapter<UserChatAdapter.UserViewHolder>() {
    var chatList: List<UserChatModel> = chatList1
    var mContext = mContext1
    var email = email1
    var name = name1
    private var auth: FirebaseAuth? = FirebaseAuth.getInstance()
    private val currentUser = auth!!.currentUser
    private val usrEmail = currentUser!!.email

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): UserViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.layout_chat_view, viewGroup, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
            if (usrEmail.equals(chatList[position].email)) {
                if (chatList[position].imageUrl == "") {
                    holder.rightChatView.visibility = View.VISIBLE
                    holder.leftChatView.visibility = View.GONE
                    holder.rlImageLeftSet.visibility = View.GONE
                    holder.rlImageRightSet.visibility = View.GONE

                    holder.setRightChatMsg.text = chatList[position].chat
                    holder.setRightChatTime.text = chatList[position].currentTime
                } else {
                    holder.rlImageRightSet.visibility = View.VISIBLE
                    holder.rightChatView.visibility = View.GONE
                    holder.leftChatView.visibility = View.GONE
                    holder.rlImageLeftSet.visibility = View.GONE

                    holder.setRightImageTime.text = chatList[position].currentTime
//                    holder.setRightChatImage.setImageBitmap(getBitmapFromURL(chatList[position].imageUrl!!))
                    Picasso.get().load(chatList[position].imageUrl).resize(150, 150)

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
                    holder.leftChatView.visibility = View.VISIBLE
                    holder.rightChatView.visibility = View.GONE
                    holder.rlImageLeftSet.visibility = View.GONE
                    holder.rlImageRightSet.visibility = View.GONE
                    holder.setLeftChatMsg.text = chatList[position].chat
                    holder.setLeftChatTime.text = chatList[position].currentTime
                } else {
                    holder.rlImageLeftSet.visibility = View.VISIBLE
                    holder.leftChatView.visibility = View.GONE
                    holder.rightChatView.visibility = View.GONE
                    holder.rlImageRightSet.visibility = View.GONE

                    holder.setLeftImageTime.text = chatList[position].currentTime
                    Picasso.get().load(chatList[position].imageUrl).resize(150, 150)
                            .centerCrop().into(holder.setLeftChatImage, object : Callback {
                                override fun onSuccess() {
                                    Log.e("Picasso ", "success")
                                }

                                override fun onError(e: Exception) {
                                    Log.e("Picasso ", "fail")
                                }
                            })
                }
            }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,
            View.OnLongClickListener {

        var rightChatView = itemView.RightChatView!!
        val setRightChatMsg = itemView.setRightChatMsg!!
        val setRightChatTime = itemView.setRightChatTime!!
        val rlImageRightSet = itemView.rl_imageRightSet!!
        val setRightChatImage = itemView.setRightChatImage!!
        val setRightImageTime = itemView.setRightImageTime!!
        //Left chat
        val leftChatView = itemView.LeftChatView!!
        val setLeftChatMsg = itemView.setLeftChatMsg!!
        val setLeftChatTime = itemView.setLeftChatTime!!
        val setLeftChatImage = itemView.setLeftChatImage!!
        val rlImageLeftSet = itemView.rl_imageLeftSet!!
        val setLeftImageTime = itemView.setLeftImageTime!!


        init {
            leftChatView.setOnLongClickListener(this)
            rightChatView.setOnLongClickListener(this)
            rlImageRightSet.setOnLongClickListener(this)
            rlImageLeftSet.setOnLongClickListener(this)
            rlImageRightSet.setOnClickListener(this)
            rlImageLeftSet.setOnClickListener(this)
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
            AlertDialog.Builder(mContext)
                    //set icon
                    //.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(msgTitle)
                    .setMessage("Are you sure to delete")
                    .setPositiveButton("Yes") { dialogInterface, i ->
                        deleteMsg(key!!)
                    }
                    .setNegativeButton("No") { dialogInterface, i ->
                        Toast.makeText(mContext, "good", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            return true
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            val key = chatList[position].key
            val intent = Intent(mContext, ImageShowActivity::class.java)
            intent.putExtra("key", key)
            intent.putExtra("email", email)
            intent.putExtra("imageSenderUser", chatList[position].email)
            intent.putExtra("name", name)
            intent.putExtra("imageUrl", chatList[position].imageUrl)
            mContext.startActivity(intent)
        }

        private fun deleteMsg(key: String) {
            val rootRef = FirebaseDatabase.getInstance().reference
            val usersdRef = rootRef.child("User")
            val userEmail1 = usrEmail!!.replace(".", "")
            val email1 = email.replace(".", "")
            usersdRef.child(userEmail1).child("chatList").child(userEmail1 + "2" + email1)
                    .child("chat").child(key).removeValue().addOnSuccessListener {
                        Log.e("deleteMsg", "success")
                    }.addOnFailureListener {
                        Log.e("deleteMsg", "fail")
                    }
        }
    }
}