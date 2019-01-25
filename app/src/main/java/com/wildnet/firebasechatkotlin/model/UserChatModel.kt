package com.wildnet.firebasechatkotlin.model

class UserChatModel(var chat: String?, var currentTime: String?, var email: String?, var key: String?,
                    var imageUrl: String?) {

    override fun toString(): String {
        return "UserChatModel(chat=$chat, currentTime=$currentTime, email=$email, key=$key, imageUrl=$imageUrl)"
    }
}
