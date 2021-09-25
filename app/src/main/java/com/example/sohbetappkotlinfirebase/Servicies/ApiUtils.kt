package com.example.sohbetappkotlinfirebase.Servicies

class ApiUtils {
    companion object{
        val BASE_URL="https://fcm.googleapis.com/fcm/"
        fun getDao():FCMDao{
            return RetrofitClient.getRetrofit(BASE_URL).create(FCMDao::class.java)
        }
    }
}