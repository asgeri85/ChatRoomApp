package com.example.sohbetappkotlinfirebase.Activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sohbetappkotlinfirebase.Adapters.MessageAdapter
import com.example.sohbetappkotlinfirebase.ChatMessage
import com.example.sohbetappkotlinfirebase.ClassModel.FCMModel
import com.example.sohbetappkotlinfirebase.ClassModel.User
import com.example.sohbetappkotlinfirebase.R
import com.example.sohbetappkotlinfirebase.Servicies.ApiUtils
import com.example.sohbetappkotlinfirebase.Servicies.FCMDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_message.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class MessageActivity : AppCompatActivity() {
    private lateinit var roomId:String
    private lateinit var dao:FCMDao
    private lateinit var messageList:ArrayList<ChatMessage>
    private lateinit var adapter:MessageAdapter
    private lateinit var mesajIdSet:HashSet<String>
    private lateinit var serverKey:String

    companion object{
         var openActivity=false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        roomId=intent.getStringExtra("room").toString()
        rvMessage.setHasFixedSize(true)
        rvMessage.layoutManager=LinearLayoutManager(this)
        dao=ApiUtils.getDao()
        readMessage(roomId)
        adapter= MessageAdapter(this@MessageActivity,messageList)
        rvMessage.adapter=adapter
        fabSendMessage.setOnClickListener {
            val message=editMessage.text.toString()
            sendMessage(roomId,message)
        }
        serverKeyRead()
        editMessage.setOnClickListener {
            rvMessage.smoothScrollToPosition(adapter.itemCount-1)
        }
    }

    private fun readMessage(id:String){
        messageList=ArrayList()
        mesajIdSet= HashSet()

        val messageRef=FirebaseDatabase.getInstance().getReference("chat_room")
        messageRef.child(id).child("chatMessage").addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    if (!mesajIdSet.contains(i.key)){
                        mesajIdSet.add(i.key.toString())
                        val message=i.getValue(ChatMessage::class.java)
                        if (message !=null){
                            messageList.add(message)
                            val sayi=snapshot.childrenCount.toInt()
                            if (openActivity)
                            messageCountUpdate(sayi)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                rvMessage.scrollToPosition(adapter.itemCount-1)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun messageCountUpdate(sayi: Int) {
        FirebaseDatabase.getInstance().getReference("room_users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("read_message").setValue(sayi)
    }

    private fun sendMessage(id:String,message:String){
        val myRef=FirebaseDatabase.getInstance().getReference("chat_room")
        if(message != ""){
            val key= myRef.child(id).child("chatMessage").push().key
            if (key!=null){
                val chatMessae=ChatMessage(message,FirebaseAuth.getInstance().currentUser!!.uid,"","",getMessageDate())
                myRef.child(id).child("chatMessage").child(key).setValue(chatMessae)
                val headers=HashMap<String,String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"]="key=$serverKey"

                FirebaseDatabase.getInstance().getReference("chat_room").child(id).child("room_users")
                    .orderByKey().addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (i in snapshot.children){
                                if(!i.key.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
                                    FirebaseDatabase.getInstance().getReference("users").orderByKey().equalTo(i.key)
                                        .addValueEventListener(object :ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val user=snapshot.children.iterator().next()
                                                val token=user.getValue(User::class.java)?.token
                                                val data=FCMModel.Data(message,"Yeni mesajınız var","mesaj",id)
                                                val not=FCMModel(data,token)
                                                dao.sendNotifaction(headers,not).enqueue(object :Callback<Response<FCMModel>>{
                                                    override fun onResponse(call: Call<Response<FCMModel>>, response: Response<Response<FCMModel>>) {
                                                        Log.e("basarili","200")
                                                    }

                                                    override fun onFailure(call: Call<Response<FCMModel>>, t: Throwable) {
                                                        Log.e("hata","404")
                                                    }

                                                })
                                                editMessage.setText("")
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }

        }

    }

    private fun getMessageDate():String{
        val sdf= SimpleDateFormat("yyyy-MM--dd HH:mm:ss", Locale("tr"))
        return sdf.format(Date())
    }

    private fun serverKeyRead(){
        val serverRef=FirebaseDatabase.getInstance().getReference("server")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    serverKey= snapshot.children.iterator().next().value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onStart() {
        super.onStart()
        openActivity=true
    }

    override fun onStop() {
        super.onStop()
        openActivity=false
    }
}