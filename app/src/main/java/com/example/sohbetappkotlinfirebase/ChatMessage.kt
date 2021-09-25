package com.example.sohbetappkotlinfirebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ChatMessage(var message:String?="",var user_id:String?="",var profil_image:String?="",var user_name:String?="",
                       var timestamp:String?="") {
}