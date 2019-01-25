package com.wildnet.firebasechatkotlin.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.activity.UserChatActivity
import com.wildnet.firebasechatkotlin.model.ShowAllGroupMemberModel
import com.wildnet.firebasechatkotlin.utility.Utility.Companion.getCurrentUser

class ShowAllGroupMemberAdapter(context: Context, userList1: List<ShowAllGroupMemberModel>, checkUserAdmin1: String, group: String) : RecyclerView.Adapter<ShowAllGroupMemberAdapter.UserViewHolder>() {
    var userList: List<ShowAllGroupMemberModel> = userList1
    val mContext = context
    private val groupName = group
    val checkUserAdmin = checkUserAdmin1

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ShowAllGroupMemberAdapter.UserViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.show_allgroup_member_adapter_layout, viewGroup, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        if (userList[position].admin.equals("admin")) {
            holder.admin.visibility = View.VISIBLE
            holder.userName.text = userList[position].name
            holder.userEmail.text = userList[position].email
            holder.admin.text = userList[position].admin
        } else {
            holder.admin.visibility = View.GONE
            holder.userName.text = userList[position].name
            holder.userEmail.text = userList[position].email
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var userName: TextView = itemView.findViewById(R.id.tv_groupUserName)
        var userEmail: TextView = itemView.findViewById(R.id.tv_groupUserEmail)
        var admin: TextView = itemView.findViewById(R.id.tv_admin)
        private var dialog: Dialog? = null

        init {
            itemView.setOnClickListener(this)

        }

        override fun onClick(v: View?) {
            val position = adapterPosition

            if (!userList[position].email.equals(getCurrentUser())) {
                dialog = Dialog(mContext)
                dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog!!.setContentView(R.layout.custom_admin_dialog)
                dialog!!.setCancelable(true)
                if (checkUserAdmin == "admin") {
                    if (userList[position].admin == "admin") {
                        dialog!!.show()
                    } else {
                        dialog!!.findViewById<TextView>(R.id.tv_dismissAdmin).visibility = View.GONE
                        dialog!!.findViewById<TextView>(R.id.tv_makeGroupAdmin).visibility = View.VISIBLE
                        dialog!!.show()
                    }
                } else {
                    AlertDialog.Builder(mContext)
                            .setTitle("Message")
                            .setMessage("Are you sure to message " + userList[position].name)
                            .setPositiveButton("Yes") { dialogInterface, i ->
                                gotoUserChat(position)
                            }
                            .setNegativeButton("No") { dialogInterface, i ->
                                //Toast.makeText(mContext, "good", Toast.LENGTH_LONG).show()
                            }
                            .show()
                }

                dialog!!.findViewById<TextView>(R.id.tv_messageUser).setOnClickListener {
                    dialog!!.dismiss()
                    val intent = Intent(mContext, UserChatActivity::class.java)
                    intent.putExtra("email", userList[position].email)
                    intent.putExtra("name", userList[position].name)
                    intent.putExtra("msgCheck",1)
                    mContext.startActivity(intent)
                    //Toast.makeText(mContext, "message user", Toast.LENGTH_LONG).show()
                }

                dialog!!.findViewById<TextView>(R.id.tv_removeUser).setOnClickListener {
                    dialog!!.dismiss()
                    fetchUserNode(position, "removeUser")
                    Toast.makeText(mContext, "remove user", Toast.LENGTH_SHORT).show()

                }

                dialog!!.findViewById<TextView>(R.id.tv_makeGroupAdmin).setOnClickListener {
                    dialog!!.dismiss()
                    fetchUserNode(position, "makeAdmin")
                    Toast.makeText(mContext, "make group admin", Toast.LENGTH_SHORT).show()

                }

                dialog!!.findViewById<TextView>(R.id.tv_dismissAdmin).setOnClickListener {
                    dialog!!.dismiss()
                    fetchUserNode(position, "dismissAsAdmin")
                    Toast.makeText(mContext, "dismiss admin", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchUserNode(position: Int, checkUser: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    if (checkUser == "removeUser") {
                        if (ds.value.toString().contains(userList[position].email!!)) {
                            removeUserToGroup(ds.key!!)
                            break
                        }
                    } else if (checkUser == "makeAdmin") {
                        if (ds.value.toString().contains(userList[position].email!!)) {
                            makeUserAdmin(ds.key!!, "admin")
                        }
                    } else {
                        if (checkUser == "dismissAsAdmin") {
                            if (ds.value.toString().contains(userList[position].email!!)) {
                                dismissAsAdmin(ds.key!!, "no")
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun dismissAsAdmin(key: String, admin: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("user").child(key).child("adminTxt")
                .setValue(admin).addOnSuccessListener {
                    Log.e("dismissAsAdmin", "success")
                }.addOnFailureListener {
                    Log.e("dismissAsAdmin", "error")
                }
    }

    private fun makeUserAdmin(key: String, admin: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("user").child(key).child("adminTxt")
                .setValue(admin)

    }

    private fun removeUserToGroup(key: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("user").child(key).removeValue()
                .addOnSuccessListener {
                    Log.e("removeUserToGroup", "success")
                    /*val intent = Intent(mContext, ShowAllGroupMemberActivity::class.java)
                    intent.putExtra("groupName", groupName)
                    mContext.startActivity(intent)*/

                }.addOnFailureListener {
                    Log.e("removeUserToGroup", "fail")
                }
    }

    private fun gotoUserChat(position: Int) {
        val intent = Intent(mContext, UserChatActivity::class.java)
        intent.putExtra("email", userList[position].email)
        intent.putExtra("name", userList[position].name)
        intent.putExtra("msgCheck",1)
        mContext.startActivity(intent)
    }
}