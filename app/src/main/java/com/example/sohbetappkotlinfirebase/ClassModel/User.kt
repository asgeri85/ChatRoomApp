package com.example.sohbetappkotlinfirebase.ClassModel

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(var name:String?="",var user_id:String?="",var telephone:String?="",var profile_image:String?="",
                var level:String?="",var token:String?="") {
}