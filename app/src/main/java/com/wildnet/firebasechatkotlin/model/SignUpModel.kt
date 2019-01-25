package com.wildnet.firebasechatkotlin.model

class SignUpModel(name: String, phone_no: String, email: String, password: String) {

    private val name: String = name
    private val password: String = password
    private val email: String = email
    private val phone: String = phone_no

    fun getName(): String? {
        return name
    }

    fun getPassword(): String? {
        return password
    }

    fun getEmail(): String? {
        return email
    }

    fun getPhone(): String? {
        return phone
    }

    override fun toString(): String {
        return "SignUpModel(name=$name, password=$password, email=$email, phone_no=$phone)"
    }
}