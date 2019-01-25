package com.wildnet.firebasechatkotlin.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.adapter.GroupListAdapter
import com.wildnet.firebasechatkotlin.model.GroupNameModel
import com.wildnet.firebasechatkotlin.utility.Utility
import kotlinx.android.synthetic.main.grouplist_layout.*

class GroupListFragment : Fragment() {

    internal lateinit var groupList: List<GroupNameModel>
    internal var auth: FirebaseAuth? = null
    private var progressBar: ProgressDialog? = null
    private lateinit var recyclerViewLayoutManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.grouplist_layout, container, false)
    }

    override fun onStart() {
        super.onStart()
        recyclerViewLayoutManager = LinearLayoutManager(activity)
        recyclerViewGroupList.layoutManager = recyclerViewLayoutManager
        getAllGroup()
    }

    private fun getAllGroup() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        progressBar = ProgressDialog(context, android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
        progressBar!!.setMessage("Fetching all data......")
        progressBar!!.setCancelable(false)
        progressBar!!.show()

        usersdRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
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



                setDataToAdapter()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressBar!!.dismiss()
            }
        })
    }

    private fun setDataToAdapter() {
        if(recyclerViewGroupList!=null) {
            if (groupList.isEmpty()) {
                recyclerViewGroupList.visibility = View.GONE
                tv_noGroup.visibility = View.VISIBLE
                progressBar!!.dismiss()
            } else {
                recyclerViewGroupList.visibility = View.VISIBLE
                tv_noGroup.visibility = View.GONE
                val groupListAdapter = GroupListAdapter(groupList, context)
                if (recyclerViewGroupList != null)
                    recyclerViewGroupList.adapter = groupListAdapter
                progressBar!!.dismiss()
            }
        }
    }
}