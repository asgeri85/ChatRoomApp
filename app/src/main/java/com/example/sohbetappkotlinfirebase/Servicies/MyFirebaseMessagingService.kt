package com.example.sohbetappkotlinfirebase.Servicies

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sohbetappkotlinfirebase.Activities.MainActivity
import com.example.sohbetappkotlinfirebase.Activities.MessageActivity
import com.example.sohbetappkotlinfirebase.ChatRoom
import com.example.sohbetappkotlinfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService:FirebaseMessagingService() {
    private var message=0
    override fun onMessageReceived(p0: RemoteMessage) {
        if (!MessageActivity.openActivity){
            val data=p0.data

            Log.e("fcm",data.toString())
            val chatID= data["sohbet_odasi_id"]
            FirebaseDatabase.getInstance().getReference("chat_room").orderByKey().equalTo(chatID)
                .addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val chatRoom=snapshot.children.iterator().next()
                        val map=(chatRoom.value as HashMap<*, *>)
                        val room= ChatRoom(map["room_id"].toString(),map["room_name"].toString(),map["room_level"].toString(),
                            map["creator_id"].toString())

                        val readMessageCount=chatRoom.child("room_users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child("read_message").value.toString().toInt()

                        val allMessageCount=chatRoom.child("chatMessage").childrenCount.toInt()
                        message=allMessageCount-readMessageCount

                        createNotifacation(data,room)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createNotifacation(data: Map<String, String>, chat:ChatRoom) {
        val notifId=createNotId(chat.room_id.toString())
        val builder:NotificationCompat.Builder
        val nofManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendinIntent=Intent(this,MainActivity::class.java)
        pendinIntent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        pendinIntent.putExtra("room_id",chat.room_id)

        val notPanding=PendingIntent.getActivity(this,20,pendinIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val kanalId=chat.room_name
            val kanalName="kanalad"
            val oncelin=NotificationManager.IMPORTANCE_HIGH
            var kanal:NotificationChannel?=nofManager.getNotificationChannel(kanalId)
            if (kanal==null){
                kanal= NotificationChannel(kanalId,kanalName,oncelin)
                nofManager.createNotificationChannel(kanal)
            }

             builder=NotificationCompat.Builder(this,kanalId!!)
             builder.setSmallIcon(R.drawable.ic_baseline_account_circle_24)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle(data["baslik"])
            .setAutoCancel(true)
            .setSubText("$message yeni mesaj")
            .setStyle(NotificationCompat.BigTextStyle().bigText(data["mesaj"]))
            .setOnlyAlertOnce(true)
            .setNumber(message).setContentIntent(notPanding)


        }else{
            builder=NotificationCompat.Builder(this)
            builder.setSmallIcon(R.drawable.ic_baseline_account_circle_24)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(data["baslik"])
                .setAutoCancel(true)
                .setSubText("$message yeni mesaj")
                .setStyle(NotificationCompat.BigTextStyle().bigText(data["mesaj"]))
                .setOnlyAlertOnce(true)
                .setContentIntent(notPanding)
                .setNumber(message).priority=Notification.PRIORITY_HIGH
        }

        nofManager.notify(notifId,builder.build())
    }

    private fun createNotId(id:String):Int{
        var nid=0
        for (i in 2..7){
            nid += id[i].toInt()
        }
        return nid
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        var messageToken:String?=""
        val token=FirebaseMessaging.getInstance().token.addOnCompleteListener {task->
            if(task.isSuccessful){
                messageToken=task.result
                Log.e("token",messageToken.toString())
            }
        }
        val ref=FirebaseDatabase.getInstance().getReference("users")
        val addToken=ref.child(FirebaseAuth.getInstance().currentUser!!.uid).child("token").setValue(messageToken)
    }
}