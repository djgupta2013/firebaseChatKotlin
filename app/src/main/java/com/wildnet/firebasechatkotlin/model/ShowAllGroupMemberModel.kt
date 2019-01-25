package com.wildnet.firebasechatkotlin.model

class ShowAllGroupMemberModel(var email: String?, var name: String?, var admin: String?) {

    override fun toString(): String {
        return "ShowAllGroupMemberModel(email=$email, name=$name, admin=$admin)"
    }
}
