package com.wildnet.firebasechatkotlin.model

class GroupChatModel(var userEmail: String?, var userName: String?, var currTime: String?, var chatMsg: String?
                     , var key: String?,var imageUrl: String?) {

    override fun toString(): String {
        return "GroupChatModel(userEmail=$userEmail, userName=$userName, currTime=$currTime, chatMsg=$chatMsg, key=$key, imageUrl=$imageUrl)"
    }
}

