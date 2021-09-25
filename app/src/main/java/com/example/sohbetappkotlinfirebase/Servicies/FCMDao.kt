package com.example.sohbetappkotlinfirebase.Servicies

import com.example.sohbetappkotlinfirebase.ClassModel.FCMModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface FCMDao {

    @POST("send")
    fun sendNotifaction(@HeaderMap headers: Map<String, String>, @Body nMessage: FCMModel):Call<Response<FCMModel>>
}