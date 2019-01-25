package com.wildnet.firebasechatkotlin.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.adapter.UserChatAdapter
import com.wildnet.firebasechatkotlin.model.UserChatModel
import com.wildnet.firebasechatkotlin.utility.Utility
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import kotlinx.android.synthetic.main.activity_user_chat.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class UserChatActivity : AppCompatActivity(), View.OnClickListener {

    var email: String = ""
    private var userEmail: String = ""
    internal var auth: FirebaseAuth? = null
    lateinit var name: String
    private var mEmojiIcons: EmojIconActions? = null
    internal lateinit var userList: List<UserChatModel>
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_chat)
        supportActionBar!!.hide()
        recyclerViewLayoutManager = LinearLayoutManager(this)
        rv_userChatHistory.layoutManager = recyclerViewLayoutManager
        auth = FirebaseAuth.getInstance()
        val currentUser = auth!!.currentUser
        if (currentUser != null) {
            userEmail = currentUser.email!!
        }
        iv_backUserList.setOnClickListener {
            if (intent.getStringExtra("imageSend") == "yes"  || intent.getIntExtra("msgCheck",0) == 1) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            super@UserChatActivity.onBackPressed()
        }
        iv_emoji.setOnClickListener(this)
        iv_messageSend.setOnClickListener(this)
        iv_imageSelect.setOnClickListener(this)
        showProfileData()
        setAllChatHistory()
        checkUserTyping()
        checkUserBlockOrUnblock()
        mEmojiIcons = EmojIconActions(this, mRootView, edittext_hani_chatbox, iv_emoji)
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
                    iv_imageSelect.visibility = View.GONE
                    Log.e("mEmojiIcons Size", "$mEmojiIcons.toString().length")
                    setUserTypingInFirebase("typing...")
                } else if (edittext_hani_chatbox.text.toString().isEmpty()) {
                    iv_imageSelect.visibility = View.VISIBLE
                    showProfileData()
                    setUserTypingInFirebase("no")
                }
            }
        })
    }

    private fun checkUserTyping() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val email1 = email.replace(".", "")
        val userEmail1 = userEmail.replace(".", "")
        usersdRef.child(email1).child("chatList").child(email1 + "2" + userEmail1)
                .child("userTyping").addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (count == 0) {
                            if (dataSnapshot.value == "typing...") {
                                tv_userLastSeen.text = dataSnapshot.value.toString()
                            } else {
                                showProfileData()
                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })
    }

    private fun setUserTypingInFirebase(typing: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val email1 = email.replace(".", "")
        val userEmail1 = userEmail.replace(".", "")
        //val typing ="typing"
        usersdRef.child(userEmail1).child("chatList").child(userEmail1 + "2" + email1)
                .child("userTyping").setValue(typing).addOnSuccessListener {

                }.addOnFailureListener {

                }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setUserTypingInFirebase("no")
        if (intent.getStringExtra("imageSend") == "yes") {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun setAllChatHistory() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val email1 = email.replace(".", "")
        val userEmail1 = userEmail.replace(".", "")
        usersdRef.child(userEmail1).child("chatList").child(userEmail1 + "2" + email1)
                .child("chat").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        userList = ArrayList()
                        Log.e("size", "" + dataSnapshot.childrenCount)
                        Log.e("dataSnapshot", "" + dataSnapshot)
                        for (ds in dataSnapshot.children) {
                            val key = ds.key
                            val chat = ds.child("chat").getValue(String::class.java)
                            val chatTime = ds.child("currentTime").getValue(String::class.java)
                            val email = ds.child("email").getValue(String::class.java)
                            val imageUrl = ds.child("imageUrl").getValue(String::class.java)
                            val userChatModel = UserChatModel(chat, chatTime, email, key, imageUrl)
                            (userList as ArrayList<UserChatModel>).add(userChatModel)
                        }
                        setDataToAdapter()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("onCancelled", "onCancelled")
                    }
                })
    }

    private fun setDataToAdapter() {
        val userChatAdapter = UserChatAdapter(this, userList, email, name)
        rv_userChatHistory.adapter = userChatAdapter
        rv_userChatHistory.scrollToPosition(rv_userChatHistory.adapter!!.itemCount - 1)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_emoji -> {
                    mEmojiIcons!!.setUseSystemEmoji(true)
                    edittext_hani_chatbox.setUseSystemDefault(true)
                }
                R.id.iv_messageSend -> {
                    Log.e("test", "ok")
                    userList = ArrayList()
                    if (edittext_hani_chatbox.text.toString().trim() != "") {
                        if (count == 0) {
                            Log.e("checkUserBlockOr", "unblock")
                            val userChatModel = UserChatModel(edittext_hani_chatbox.text.toString().trim(),
                                    Utility.getCurrTime(), userEmail, "", "")
                            Log.e("", "")
                            (userList as ArrayList<UserChatModel>).add(userChatModel)
                            setDataToAdapter()
                            setChatToFirebase(userChatModel)
                            edittext_hani_chatbox.setText("")
                        } else {
                            Log.e("checkUserBlockOr", "block")
                            AlertDialog.Builder(this)
                                    .setCancelable(false)
                                    .setMessage("Unblock  $name to send a message")
                                    .setPositiveButton("UNBLOCK") { dialogInterface, i ->
                                        val userChatModel = UserChatModel(edittext_hani_chatbox.text.toString().trim(),
                                                Utility.getCurrTime(), userEmail, "", "")
                                        Log.e("", "")
                                        (userList as ArrayList<UserChatModel>).add(userChatModel)
                                        setDataToAdapter()
                                        setChatToFirebase(userChatModel)
                                        edittext_hani_chatbox.setText("")
                                        blockOrUnblockUser("unblock")
                                        mToolbar.menu.findItem(R.id.action_userUnblock).isVisible = false
                                        mToolbar.menu.findItem(R.id.action_userBlock).isVisible = true
                                    }
                                    .setNegativeButton("CANCEL") { dialogInterface, i ->
                                    }
                                    .show()
                        }
                    } else {
                        edittext_hani_chatbox.error
                    }
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
        if (requestCode == GALLERY && resultCode == Activity.RESULT_OK) {
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
        } else if (requestCode == CAMERA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val photo: Bitmap = data.extras.get("data") as Bitmap
                val thumbnail = data.extras!!.get("data") as Bitmap

                //imageview!!.setImageBitmap(thumbnail)
                val path = saveImage(thumbnail)
                saveImageToFirebase(path)
                Log.e("saveImage", "$path")
            }
        }
    }

    private val IMAGE_DIRECTORY = "/test"
    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
                (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        // have the object build the directory structure, if needed.
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists()) {

            wallpaperDirectory.mkdirs()
        }

        try {
            Log.d("heel", wallpaperDirectory.toString())
            val f = File(wallpaperDirectory, ((Calendar.getInstance()
                    .getTimeInMillis()).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this,
                    arrayOf(f.getPath()),
                    arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())

            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

    private fun saveImageToFirebase(picturePath: String) {
        val mStorageRef = FirebaseStorage.getInstance().reference
        val file = Uri.fromFile(File((picturePath)))
        val riversRef = mStorageRef.child(file.lastPathSegment)
        riversRef.putFile(file).addOnSuccessListener {

            riversRef.downloadUrl.addOnSuccessListener {
                userList = ArrayList()
                val urlPath = it.toString()
                val userChatModel = UserChatModel("",
                        Utility.getCurrTime(), userEmail, "", urlPath)
                Log.e("", "")
                (userList as ArrayList<UserChatModel>).add(userChatModel)
                setChatToFirebase(userChatModel)
                setDataToAdapter()
                Log.e("urlPath", "$urlPath")
            }
            Log.e("saveImageToFirebase", "" + it)
        }.addOnCanceledListener {
            Log.e("saveImageToFirebase", "fail")
        }


    }

    private fun setChatToFirebase(userChatModel: UserChatModel) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val email1 = email.replace(".", "")
        val userEmail1 = userEmail.replace(".", "")
        usersdRef.child(userEmail1).child("chatList").child(userEmail1 + "2" + email1)
                .child("chat").child(Date().time.toString()).setValue(userChatModel)
                .addOnSuccessListener {
                    Log.e("Success", "success")
                }.addOnFailureListener {
                    Log.e("fail", "fail")
                }
        usersdRef.child(email1).child("chatList").child(email1 + "2" + userEmail1)
                .child("chat").child(Date().time.toString()).setValue(userChatModel)
    }

    private fun showProfileData() {
        name = intent.getStringExtra("name")
        email = intent.getStringExtra("email")
        Log.e("name", "$name $email")
        tv_userChatName.text = name

        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val email1: String = email.replace(".", "")
        val userEmail1 = userEmail.replace(".", "")
        usersdRef.child(userEmail1).child("chatList").child(userEmail1 + "2" + email1)
                .child("userBlock").addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.value == "unblock" || dataSnapshot.value == null) {
                            Log.e("dataSnapshot", dataSnapshot.toString())
                            showUserOnline()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
    }

    fun showUserOnline() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val email1: String = email.replace(".", "")
        usersdRef.child(email1).child("activeUser").addValueEventListener(object : ValueEventListener {
            @SuppressLint("SimpleDateFormat", "SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.e("dataSnapshot", dataSnapshot.toString())
                if (dataSnapshot.value == "online") {
                    tv_userLastSeen.text = dataSnapshot.value as CharSequence?
                } else {
                    val date = Date(dataSnapshot.getValue(Long::class.java)!!)
                    val format = SimpleDateFormat("EEE hh:mm a")
                    tv_userLastSeen.text = "last seen : " + format.format(date)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mToolbar.inflateMenu(R.menu.menu_user_chat)
        Log.e("onCreateOptionsMenu", "onCreateOptionsMenu")
        if (count == 1) {
            mToolbar.menu.findItem(R.id.action_userUnblock).isVisible = true
            mToolbar.menu.findItem(R.id.action_userBlock).isVisible = false
        } else {
            mToolbar.menu.findItem(R.id.action_userUnblock).isVisible = false
            mToolbar.menu.findItem(R.id.action_userBlock).isVisible = true
        }
        mToolbar.setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_userDetail -> {
                Toast.makeText(this, "User Detail", Toast.LENGTH_SHORT).show()
            }
            R.id.action_userBlock -> {
                AlertDialog.Builder(this)
                        .setTitle("Block")
                        .setMessage("Are you sure to block $name")
                        .setPositiveButton("Yes") { dialogInterface, i ->
                            item.isVisible = false
                            mToolbar.menu.findItem(R.id.action_userUnblock).isVisible = true
                            blockOrUnblockUser("block")
                        }
                        .setNegativeButton("No") { dialogInterface, i ->
                            Toast.makeText(this, "good", Toast.LENGTH_LONG).show()
                        }
                        .show()
            }
            R.id.action_userUnblock -> {
                AlertDialog.Builder(this)
                        .setTitle("Unblock")
                        .setMessage("Are you sure to unblock $name")
                        .setPositiveButton("Yes") { dialogInterface, i ->
                            item.isVisible = false
                            mToolbar.menu.findItem(R.id.action_userBlock).isVisible = true
                            blockOrUnblockUser("unblock")
                        }
                        .setNegativeButton("No") { dialogInterface, i ->
                            Toast.makeText(this, "good", Toast.LENGTH_LONG).show()
                        }
                        .show()
            }
        }
        return true
    }

    private fun blockOrUnblockUser(blockOrunblock: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val email1 = email.replace(".", "")
        val userEmail1 = userEmail.replace(".", "")
        usersdRef.child(email1).child("chatList").child(email1 + "2" + userEmail1)
                .child("userBlock").setValue(blockOrunblock).addOnSuccessListener {
                    Log.e("blockUser", "yes $blockOrunblock")
                }.addOnFailureListener {
                    Log.e("blockUser", "no $it")
                }
    }

    var count: Int = 5
    private fun checkUserBlockOrUnblock(): Boolean {
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("User")
        val email1 = email.replace(".", "")
        val userEmail1 = userEmail.replace(".", "")
        var boolean = false
        usersdRef.child(userEmail1).child("chatList").child(userEmail1 + "2" + email1)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // boolean = dataSnapshot.value != "block"
                        Log.e("checkUserBlockOrUnblock", "checkUserBlockOrUnblock")
                        val check = dataSnapshot.child("userBlock").getValue(String::class.java)
                        if (check == "block") {
                            tv_userLastSeen.text = ""
                            tv_blockUser.visibility = View.VISIBLE
                            tv_blockUser.text = "you are block"
                            boolean = false
                        } else {
                            showProfileData()
                            Log.e("checkUserBlockOrUnblock", "showProfileData")
                            tv_blockUser.visibility = View.GONE
                            boolean = true
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })

        usersdRef.child(email1).child("chatList").child(email1 + "2" + userEmail1)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // boolean = dataSnapshot.value != "block"
                        Log.e("checkUserBlockOrUnblock", "checkUserBlockOrUnblock")
                        val check = dataSnapshot.child("userBlock").getValue(String::class.java)
                        if (check == "block") {
                            count = 1
                            tv_userLastSeen.text = ""
                            Log.e("count 1", count.toString())
                        } else {
                            count = 0
                            Log.e("count 0", count.toString())
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })
        return boolean
    }
}