package com.example.sohbetappkotlinfirebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ChatRoom(var room_id:String?="",var room_name:String?="",var room_level:String?="",var creator_id:String?="",
var chatMessage:List<ChatMessage>?=null) {
}