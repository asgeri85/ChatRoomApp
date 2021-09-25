package com.example.sohbetappkotlinfirebase.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sohbetappkotlinfirebase.Adapters.RoomAdapter
import com.example.sohbetappkotlinfirebase.ChatMessage
import com.example.sohbetappkotlinfirebase.ChatRoom
import com.example.sohbetappkotlinfirebase.ClassModel.User
import com.example.sohbetappkotlinfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    private lateinit var allRooms:ArrayList<ChatRoom>
    private lateinit var adapter:RoomAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        rvChatRoom.setHasFixedSize(true)
        rvChatRoom.layoutManager=LinearLayoutManager(this)
        fabAddChat.setOnClickListener {
            alertOpen()
        }
        readAllRooms()

    }

    private fun alertOpen(){
        val view=layoutInflater.inflate(R.layout.alert_chat_room,null)
        val editName=view.findViewById(R.id.editAlertChatName) as EditText
        val seekBar=view.findViewById(R.id.seekBar) as SeekBar
        val textSeekbar=view.findViewById(R.id.textViewAlertRoomLevel) as TextView
        var level=0
        val ad=AlertDialog.Builder(this@ChatActivity)
        ad.setTitle("Sohbet odası oluşturma")
        ad.setMessage("Oda bilgilerini giriniz")
        ad.setView(view)

        val myRef=FirebaseDatabase.getInstance().getReference("users")
        val query=myRef.orderByKey().equalTo(FirebaseAuth.getInstance().currentUser?.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (u in snapshot.children){
                    val user=u.getValue(User::class.java)
                    if (user != null){
                        level=user.level!!.toInt()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                textSeekbar.text="Seviyye:  $p1"
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        ad.setPositiveButton("Oluştur"){dialog,i->
            val name=editName.text.toString().trim()
            val seekbarValue=seekBar.progress
            if (name.isNotEmpty() && seekbarValue.toString().isNotEmpty()){
                if (level >=seekbarValue){
                    val chatRef=FirebaseDatabase.getInstance().getReference("chat_room")
                    val key=chatRef.push().key
                    if(key != null){
                        val chatRoom= ChatRoom(key,name,seekbarValue.toString(),FirebaseAuth.getInstance().currentUser?.uid)
                        chatRef.child(key).setValue(chatRoom).addOnCompleteListener { task->
                            if (task.isSuccessful){
                                val messageKey=chatRef.push().key
                                if (messageKey!=null){
                                    val chatMessage= ChatMessage()
                                    chatMessage.message="sohbet odasına hoş geldiniz"
                                    chatMessage.timestamp=getMessageDate()
                                    chatRef.child(key).child("chatMessage").child(messageKey).setValue(chatMessage)
                                        .addOnCompleteListener { task->
                                        if(task.isSuccessful){
                                            readAllRooms()
                                            Toast.makeText(applicationContext,"Oda başarılı bir şekilde oluşturuldu",
                                                Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                }
                            }else{
                                Toast.makeText(applicationContext,"Hata:${task.exception?.message}",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else{
                    Toast.makeText(applicationContext,"Seviyeniz: $level\nSeviyenizden yüksek seviyeli sohbet odası oluşturamazsınız",
                    Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(applicationContext,"Gerekli alanları doldurunuz",Toast.LENGTH_SHORT).show()
            }
        }

        ad.setNegativeButton("iptal"){ _,_ ->
        }
        ad.create().show()

    }

    private fun getMessageDate():String{
        val sdf=SimpleDateFormat("yyyy-MM--dd HH:mm:ss", Locale("tr"))
        return sdf.format(Date())
    }

     fun readAllRooms(){
        progressBarChat.visibility=View.VISIBLE
        allRooms= ArrayList()
        val myRef=FirebaseDatabase.getInstance().getReference("chat_room")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val map=(i.value as HashMap<*, *>)
                    val room= ChatRoom(map["room_id"].toString(),map["room_name"].toString(),map["room_level"].toString(),
                        map["creator_id"].toString())

                    val allMesage=ArrayList<ChatMessage>()
                    for (m in i.child("chatMessage").children){
                        val mesage=m.getValue(ChatMessage::class.java)
                        if (mesage!=null){
                            allMesage.add(mesage)
                        }else{
                            progressBarChat.visibility=View.INVISIBLE
                            Toast.makeText(applicationContext,"Oda yok",Toast.LENGTH_SHORT).show()
                        }
                    }
                    room.chatMessage=allMesage
                    allRooms.add(room)
                    progressBarChat.visibility=View.INVISIBLE
                }
                adapter=RoomAdapter(this@ChatActivity,allRooms)
                rvChatRoom.adapter=adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}