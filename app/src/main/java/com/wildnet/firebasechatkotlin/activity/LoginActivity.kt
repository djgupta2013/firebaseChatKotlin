package com.wildnet.firebasechatkotlin.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.wildnet.firebasechatkotlin.R
import com.wildnet.firebasechatkotlin.utility.Utility

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    internal lateinit var login: Button
    private var signUp: Button? = null
    private lateinit var emailEt: EditText
    private var passwordEt: EditText? = null
    private var auth: FirebaseAuth? = null
    private var progressBar: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth!!.currentUser
        if (currentUser != null) {
            Log.e("email ", "" + currentUser.email)
            val i = Intent(baseContext, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        Log.e("mAuth", "mAuth")
        this.findViewById()
        signUp!!.setOnClickListener(this)
        login.setOnClickListener(this)
    }

    private fun findViewById() {
        login = findViewById(R.id.btn_signIn)
        signUp = findViewById(R.id.btn_signUpActivity)
        passwordEt = findViewById(R.id.et_passwordLogin)
        emailEt = findViewById(R.id.et_emailLogin)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_signIn -> {
                val email: String = emailEt.text.toString().trim()
                val password: String = passwordEt!!.text.toString().trim()
                if (checkValidation(email, password)) {
                    progressBar = ProgressDialog(this@LoginActivity, android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    progressBar!!.setMessage("Please wait......")
                    progressBar!!.setCancelable(false)
                    progressBar!!.show()
                    userLogin(email, password)
                }
            }
            R.id.btn_signUpActivity -> {
                val i = Intent(baseContext, SignUpActivity::class.java)
                startActivity(i)
            }
        }
    }

    private fun checkValidation(email: String, password: String): Boolean {
        if (email == "") {
            Toast.makeText(this, "Email id must be require", Toast.LENGTH_SHORT).show()
            return false
        } else if (!Utility.isValidEmail(email)) {
            Toast.makeText(this, "Please enter valid email id", Toast.LENGTH_SHORT).show()
            return false
        } else if (password == "") {
            Toast.makeText(this, "Password must be require", Toast.LENGTH_SHORT).show()
        } else if (password.length < 5) {
            Toast.makeText(this, "Password must be 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun userLogin(email: String, password: String) {
        auth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.e("userLogin", "success")
                        progressBar!!.dismiss()
                        val i = Intent(baseContext, MainActivity::class.java)
                        startActivity(i)
                        finish()
                    } else {
                        progressBar!!.dismiss()
                        Toast.makeText(this, "Please enter correct email and password", Toast.LENGTH_SHORT).show()
                        Log.e("userLogin", "fail ")
                    }
                }
    }
}
