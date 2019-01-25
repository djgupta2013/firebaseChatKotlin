package com.wildnet.firebasechatkotlin.model

class UserDetailModel(emailcheck: String, name1: String) {

    var name: String = name1
        internal set
    var email: String = emailcheck
        internal set

    override fun toString(): String {
        return "UserDetailModel{" +
                "name='" + name + '\''.toString() +
                ", email='" + email + '\''.toString() +
                '}'.toString()
    }
}
