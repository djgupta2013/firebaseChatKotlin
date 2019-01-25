package com.wildnet.firebasechatkotlin.activity


import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.adapter.GroupChatAdapter
import com.wildnet.firebasechatkotlin.model.GroupChatModel
import com.wildnet.firebasechatkotlin.model.GroupTypingModel
import com.wildnet.firebasechatkotlin.utility.Utility.Companion.getCurrTime
import com.wildnet.firebasechatkotlin.utility.Utility.Companion.getCurrentUser
import com.wildnet.firebasechatkotlin.utility.Utility.Companion.getUserName
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import kotlinx.android.synthetic.main.activity_group_chat.*
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
class GroupChatActivity : AppCompatActivity(), View.OnClickListener {
    private var groupName = ""
    lateinit var chatList: List<GroupChatModel>
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private var mEmojiIcons: EmojIconActions? = null
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)
        supportActionBar!!.hide()
//        val intent = intent
        groupName = intent.getStringExtra("groupName")
        Log.e("group name", groupName)
        tv_groupChatName.text = groupName.substring(0, (groupName.length - 2))
        getGroupChatHistory()
        checkUserExits()
        setGroupMemberToToolbar()
        checkUserTyping()
        recyclerViewLayoutManager = LinearLayoutManager(applicationContext)
        rv_groupChatHistory.layoutManager = recyclerViewLayoutManager
        iv_groupMessageSend.setOnClickListener(this)
        iv_groupEmoji.setOnClickListener(this)
        tv_groupMember.setOnClickListener(this)
        tv_groupChatName.setOnClickListener(this)
        iv_imageSelect.setOnClickListener(this)
        iv_backGroupList.setOnClickListener {
            val groupTypingModel = GroupTypingModel(getCurrentUser(), getUserName(this@GroupChatActivity),
                    "no")
            setUserTypingInFirebase(groupTypingModel)
            if(intent.getStringExtra("sendImage")=="yes") {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }else {
                super@GroupChatActivity.onBackPressed()
            }
        }
        mEmojiIcons = EmojIconActions(this, mGroupRootView, edittext_hani_chatbox, iv_groupEmoji)
        mEmojiIcons!!.ShowEmojIcon()
        mEmojiIcons!!.setKeyboardListener(object : EmojIconActions.KeyboardListener {
            override fun onKeyboardOpen() {
                Log.e("Keyboard", "open")
            }

            override fun onKeyboardClose() {
                Log.e("Keyboard", "close")
            }
        })
        mEmojiIcons!!.addEmojiconEditTextList(edittext_hani_chatbox)

        edittext_hani_chatbox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.e("afterTextChanged", "afterTextChanged")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.e("beforeTextChanged", "beforeTextChanged")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.e("onTextChanged", "onTextChanged")
                if (!TextUtils.isEmpty(edittext_hani_chatbox.text.toString()) &&
                        edittext_hani_chatbox.text.toString().length == 1) {
                    iv_imageSelect.visibility=View.GONE
                    val groupTypingModel = GroupTypingModel(getCurrentUser(), getUserName(this@GroupChatActivity), "typing")
                    setUserTypingInFirebase(groupTypingModel)
                } else if (edittext_hani_chatbox.text.toString().isEmpty()) {
                    setGroupMemberToToolbar()
                    iv_imageSelect.visibility=View.VISIBLE
                    val groupTypingModel = GroupTypingModel(getCurrentUser(), getUserName(this@GroupChatActivity), "no")
                    setUserTypingInFirebase(groupTypingModel)
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val groupTypingModel = GroupTypingModel(getCurrentUser(), getUserName(this@GroupChatActivity),
                "no")
        setUserTypingInFirebase(groupTypingModel)
        if(intent.getStringExtra("sendImage")=="yes") {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun checkUserTyping() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("userTyping").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userEmail = dataSnapshot.child("userEmail").getValue(String::class.java)
                val typing = dataSnapshot.child("typing").getValue(String::class.java)
                val name = dataSnapshot.child("name").getValue(String::class.java)
                if (!userEmail.equals(getCurrentUser()) && typing.equals("typing")) {
                    tv_groupMember.text = name + " typing....."
                } else {
                    setGroupMemberToToolbar()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    private fun setUserTypingInFirebase(groupTypingModel: GroupTypingModel) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("userTyping").setValue(groupTypingModel)
                .addOnSuccessListener {

                }.addOnFailureListener {
                }
    }

    private fun setGroupMemberToToolbar() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val groupMemberList = ArrayList<String>()
                for (ds in dataSnapshot.children) {
                    val name = ds.child("name").getValue(String::class.java).toString()
                    groupMemberList.add(name)
                }
                if (!groupMemberList.isEmpty() && groupMemberList.size > 1) {
                    val name = groupMemberList[0] + " , " + groupMemberList[1] + ". . . . . ."
                    tv_groupMember.text = name
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun setMenuForAdmin() {
        mToolbarGroup.menu.findItem(R.id.action_deleteGroup).isVisible = true
    }

    private fun setMenuForNotAdmin() {
        mToolbarGroup.menu.findItem(R.id.action_deleteGroup).isVisible = false
    }

    private fun getGroupChatHistory() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("chatList")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        chatList = ArrayList()
                        for (ds in dataSnapshot.children) {
                            val key = ds.key
                            val chatMsg = ds.child("chatMsg").getValue(String::class.java)
                            val userEmail = ds.child("userEmail").getValue(String::class.java)
                            val currTime = ds.child("currTime").getValue(String::class.java)
                            val userName = ds.child("userName").getValue(String::class.java)
                            val imageUrl=ds.child("imageUrl").getValue(String::class.java)
                            val groupChatModel = GroupChatModel(userEmail, userName, currTime, chatMsg, key,imageUrl)
                            (chatList as ArrayList<GroupChatModel>).add(groupChatModel)
                        }
                        setDataToAdapter()
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_groupMessageSend -> {
                    chatList = ArrayList()
                    if (edittext_hani_chatbox.text.toString().trim() != "") {
                        val groupChatModel = GroupChatModel(getCurrentUser(), getUserName(this), getCurrTime(),
                                edittext_hani_chatbox.text.toString().trim(), "","")
                        (chatList as ArrayList<GroupChatModel>).add(groupChatModel)
                        setDataToAdapter()
                        setGroupChatToFirebase(groupChatModel)
                        edittext_hani_chatbox.setText("")
                    }
                }
                R.id.iv_groupEmoji -> {
                    mEmojiIcons!!.setUseSystemEmoji(true)
                    edittext_hani_chatbox.setUseSystemDefault(true)
                }
                R.id.tv_groupChatName -> {
                    val intent = Intent(this, ShowAllGroupMemberActivity::class.java)
                    intent.putExtra("groupName", groupName)
                    startActivity(intent)
                }
                R.id.tv_groupMember -> {
                    val intent = Intent(this, ShowAllGroupMemberActivity::class.java)
                    intent.putExtra("groupName", groupName)
                    startActivity(intent)
                }
                R.id.iv_imageSelect -> {
                    showPictureDialog()
                }
            }
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        /* if (resultCode == this.RESULT_CANCELED)
         {
         return
         }*/
        if (requestCode == GALLERY) {
            val selectedImage = data!!.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage,
                    filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
            saveImageToFirebase(picturePath)
        } else if (requestCode == CAMERA) {

        }
    }

    private fun saveImageToFirebase(picturePath: String?) {
        val mStorageRef = FirebaseStorage.getInstance().reference
        val file = Uri.fromFile(File((picturePath)))
        val riversRef = mStorageRef.child(file.lastPathSegment)
        riversRef.putFile(file).addOnSuccessListener { it ->

            riversRef.downloadUrl.addOnSuccessListener {
                chatList = ArrayList()
                val urlPath = it.toString()
                val groupChatModel = GroupChatModel(getCurrentUser(), getUserName(this), getCurrTime(),
                        "", "",urlPath)
                (chatList as ArrayList<GroupChatModel>).add(groupChatModel)
                setDataToAdapter()
                setGroupChatToFirebase(groupChatModel)
                Log.e("urlPath", "$urlPath")
            }
            Log.e("saveImageToFirebase", "" + it)
        }
    }

    private fun setGroupChatToFirebase(groupChatModel: GroupChatModel) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("chatList").child(Date().time.toString()).setValue(groupChatModel)
                .addOnSuccessListener {
                    Log.e("setGroupChatToFirebase", "success")
                }.addOnFailureListener {
                    Log.e("setGroupChatToFirebase", "fail")
                }
    }

    private fun setDataToAdapter() {
        val groupChatAdapter = GroupChatAdapter(this, chatList, groupName)
        rv_groupChatHistory.adapter = groupChatAdapter
        rv_groupChatHistory.scrollToPosition(rv_groupChatHistory.adapter!!.itemCount - 1)
    }

    private var menuItem: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mToolbarGroup.inflateMenu(R.menu.menu_group_chat)
        menuItem = menu.findItem(R.id.action_leaveUser)
        mToolbarGroup.setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }
        checkUserAdmin()
        return true
    }

    private fun checkUserAdmin() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        var adminCheck = true
        usersdRef.child(groupName).child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val admin = ds.child("adminTxt").getValue(String::class.java).toString()
                    val email = ds.child("email").getValue(String::class.java).toString()
                    if (getCurrentUser() == email && admin == "admin") {
                        setMenuForAdmin()
                        adminCheck = false
                        break
                    } else {
                        adminCheck = true
                    }
                }
                if (adminCheck) {
                    setMenuForNotAdmin()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    var yes: String = ""
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_leaveUser -> {
                AlertDialog.Builder(this)
                        .setTitle("Leave")
                        .setMessage("Are you sure to leave this group")
                        .setPositiveButton("Yes") { dialogInterface, i ->
                            Log.e("AlertDialog Leave", "$dialogInterface $i")
                            yes = "yes"
                            leaveGroup()
                        }
                        .setNegativeButton("No") { dialogInterface, i ->
                            Log.e("AlertDialog Leave", "$dialogInterface $i")
                            Toast.makeText(this, "good", Toast.LENGTH_LONG).show()
                        }
                        .show()
                Toast.makeText(this, "User Detail", Toast.LENGTH_SHORT).show()
            }
            R.id.action_userDetail -> {
                val intent = Intent(this, ShowAllGroupMemberActivity::class.java)
                intent.putExtra("groupName", groupName)
                startActivity(intent)
            }
            R.id.action_deleteGroup -> {
                AlertDialog.Builder(this)
                        .setTitle("Delete Group")
                        .setMessage("Are you sure to delete group ")
                        .setPositiveButton("Yes") { dialogInterface, i ->
                            deleteGroup()
                        }
                        .setNegativeButton("No") { dialogInterface, i ->
                            Toast.makeText(this, "good", Toast.LENGTH_LONG).show()
                        }
                        .show()
            }
        }
        return true
    }

    private fun deleteGroup() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")

        usersdRef.child(groupName).removeValue().addOnSuccessListener {
            Log.e("deleteGroup", "success")
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Log.e("deleteGroup", "fail")
        }
    }

    private fun checkUserExits() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    if (ds.value.toString().contains(getCurrentUser()!!)) {
                        if (ds.child("userActive").getValue(String::class.java).equals("no")) {
                            Log.e("leaveGroup", "leaveGroup")
                            ll_sendMsg.visibility = View.GONE
                        } else {
                            ll_sendMsg.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun leaveGroup() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    if (ds.value.toString().contains(getCurrentUser()!!)) {
                        if (yes != "" && yes == "yes") {
                            exitGroup(ds.key!!)
                            yes = ""
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun exitGroup(key: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Group")
        usersdRef.child(groupName).child("user").child(key).removeValue()
                .addOnSuccessListener {
                    Log.e("exitGroup", "success")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Log.e("exitGroup", "fail")
                }
    }
}