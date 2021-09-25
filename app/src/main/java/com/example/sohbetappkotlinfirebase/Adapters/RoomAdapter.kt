package com.example.sohbetappkotlinfirebase.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.sohbetappkotlinfirebase.Activities.ChatActivity
import com.example.sohbetappkotlinfirebase.Activities.MessageActivity
import com.example.sohbetappkotlinfirebase.ChatRoom
import com.example.sohbetappkotlinfirebase.ClassModel.User
import com.example.sohbetappkotlinfirebase.R
import com.example.sohbetappkotlinfirebase.databinding.CardChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class RoomAdapter(var mContext:Context,var roomList:ArrayList<ChatRoom>):
    RecyclerView.Adapter<RoomAdapter.RoomHolder>() {
    inner class RoomHolder(var view:CardChatBinding):RecyclerView.ViewHolder(view.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomHolder {
        val layout=DataBindingUtil.inflate<CardChatBinding>(LayoutInflater.from(mContext), R.layout.card_chat,parent,false)
        return RoomHolder(layout)
    }

    override fun onBindViewHolder(holder: RoomHolder, position: Int) {
        holder.view.oda=roomList[position]
        val room=roomList[position]
        val myRef=FirebaseDatabase.getInstance().getReference("users")
        myRef.orderByKey().equalTo(room.creator_id).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val user=i.getValue(User::class.java)
                    if(user!=null){
                        Picasso.get().load(user.profile_image).resize(100,100).into(holder.view.imageViewCardRoom);
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        if (room.creator_id!!.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
            holder.view.imageButtonRoomDelete.visibility=View.VISIBLE
            holder.view.imageButtonRoomDelete.setOnClickListener {
                alertOpen(room)
            }
        }

        holder.view.cardRoom.setOnClickListener {
            saveUserRoom(room)
            val intent=Intent(mContext,MessageActivity::class.java)
            intent.putExtra("room",room.room_id)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    private fun alertOpen(room:ChatRoom){
        val ad=AlertDialog.Builder(mContext)
        ad.setTitle("Oda Silme")
        ad.setMessage("Odayı silmek istiyor musunuz?")
        ad.setPositiveButton("Evet"){d,i->
            val myRef=FirebaseDatabase.getInstance().getReference("chat_room")
            myRef.child(room.room_id.toString()).removeValue().addOnCompleteListener { task->
                if (task.isSuccessful){
                    Toast.makeText(mContext,"Oda başarılı bir şekilde silindi",Toast.LENGTH_SHORT).show()
                    (mContext as ChatActivity).readAllRooms()
                }else{
                    Toast.makeText(mContext,"Hata: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                }
            }
        }
       ad.setNegativeButton("İptal"){_,_->

       }
       ad.setCancelable(true)
       ad.create().show()
    }

    private fun saveUserRoom(room:ChatRoom){
        val roomRef=FirebaseDatabase.getInstance().getReference("chat_room")
        roomRef.child(room.room_id.toString()).child("room_users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("read_message").setValue(room.chatMessage?.size.toString())
    }
}