package com.wildnet.firebasechatkotlin.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.model.UserModel
import kotlinx.android.synthetic.main.user_adapter_layout.view.*

class CreateGroupAdapter(userModelList: List<UserModel>) : RecyclerView.Adapter<CreateGroupAdapter.UserViewHolder>() {

    private var userList=userModelList
     private lateinit var listener: CheckBoxData

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): UserViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.user_adapter_layout, viewGroup, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, p1: Int) {
        holder.userName.text=userList[p1].name
        holder.userEmail.text=userList[p1].email
    }

    fun setListener(listener: CheckBoxData) {
        this.listener = listener
    }

    interface CheckBoxData {
        fun checkData(position: Int, isCheck: Boolean)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var checkUser= itemView.chb_user!!
        var userName= itemView.tv_userName!!
        var userEmail= itemView.tv_userEmail!!

        init {
            checkUser.visibility=View.VISIBLE
            checkUser.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            val model = userList[position]
            if(model.isSelected){
                model.isSelected=false
                listener.checkData(position, false)
            }else{
                model.isSelected=true
                listener.checkData(position, true)
            }
        }
    }
}