package com.example.sohbetappkotlinfirebase.ClassModel

import com.google.gson.annotations.SerializedName

data class FCMModel(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("to")
	val to: String? = null
){
	 data class Data(

		@field:SerializedName("mesaj")
		val mesaj: String? = null,

		@field:SerializedName("baslik")
		val baslik: String? = null,

		@field:SerializedName("bildirim_turu")
		val bildirimTuru: String? = null,

		@field:SerializedName("sohbet_odasi_id")
		val room_id: String? = null
	)
}


