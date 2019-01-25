package com.wildnet.firebasechatkotlin.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.model.SignUpModel
import com.wildnet.firebasechatkotlin.utility.Utility
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    private var mAuth: FirebaseAuth? = null
    private var progressBar: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        mAuth = FirebaseAuth.getInstance()
        btn_signUp!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val name: String = et_nameSignUp.text.toString().trim()
        val number: String = et_numberSignUp.text.toString().trim()
        val email: String = et_emailSignUp!!.text.toString().trim()
        val pass: String = et_passSignUp!!.text.toString().trim()
        if (checkValidation()) {
            if (Utility.isOnline(this)) {
                progressBar = ProgressDialog(this@SignUpActivity, android.app.AlertDialog
                        .THEME_DEVICE_DEFAULT_LIGHT)
                progressBar!!.setMessage("Please wait......")
                progressBar!!.setCancelable(false)
                progressBar!!.show()
                val signUpModel = SignUpModel(name, number, email, pass)
                Log.e("test", "test")
                saveData(signUpModel)
            }
        }
    }

    private fun saveData(signUpModel: SignUpModel) {
        mAuth!!.createUserWithEmailAndPassword(signUpModel.getEmail()!!, signUpModel.getPassword()!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.e("Success", "Sign up success")
                        saveDataOnFirebase(signUpModel)
                    } else {
                        progressBar!!.dismiss()
                        Log.e("Fail", "Sign up fail")
                    }
                }
    }

    private fun saveDataOnFirebase(signUpModel: SignUpModel) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference
        var email: String = signUpModel.getEmail()!!
        email = email.replace(".", "")
        myRef.child("User").child(email).setValue(signUpModel).addOnSuccessListener {
            Log.e("saveDataOnFirebase", "saveDataOnFirebase success")
            progressBar!!.dismiss()
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
            finish()
        }.addOnFailureListener {
            progressBar!!.dismiss()
            Log.e("saveDataOnFirebase", "saveDataOnFirebase fail")
        }
    }

    private fun checkValidation(): Boolean {
        if (et_nameSignUp.text.toString().trim() == "") {
            Toast.makeText(this, "User name must be require", Toast.LENGTH_SHORT).show()
            return false
        } else if (et_numberSignUp.text.toString().trim() == "") {
            Toast.makeText(this, "Mobile number must be require", Toast.LENGTH_SHORT).show()
        } else if (et_numberSignUp.text.toString().trim().length < 10) {
            Toast.makeText(this, "Mobile number must be of 10 digits", Toast.LENGTH_SHORT).show()
            return false
        } else if (et_emailSignUp!!.text.toString().trim() == "") {
            Toast.makeText(this, "Email id must be require", Toast.LENGTH_SHORT).show()
            return false
        } else if (!Utility.isValidEmail(et_emailSignUp!!.text.toString().trim())) {
            Toast.makeText(this, "Please enter valid email id", Toast.LENGTH_SHORT).show()
            return false
        } else if (et_passSignUp!!.text.toString().trim() == "") {
            Toast.makeText(this, "Password must be require", Toast.LENGTH_SHORT).show()
        } else if (et_passSignUp!!.text.toString().trim().length < 5) {
            Toast.makeText(this, "Password must be 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
