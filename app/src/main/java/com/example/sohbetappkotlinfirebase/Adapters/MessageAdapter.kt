package com.example.sohbetappkotlinfirebase.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sohbetappkotlinfirebase.ChatMessage
import com.example.sohbetappkotlinfirebase.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(var mContext:Context,var listMessage:ArrayList<ChatMessage>):
    RecyclerView.Adapter<MessageAdapter.MessageHolder>() {
    inner class MessageHolder(var view:View):RecyclerView.ViewHolder(view){
        var message1:TextView
        var date1:TextView
        init {
            message1=view.findViewById(R.id.textViewMessage1)
            date1=view.findViewById(R.id.textViewMessageDate1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        var layout:View?
        layout = if (viewType==1){
            LayoutInflater.from(mContext).inflate(R.layout.card_message_2,parent,false)
        }else{
            LayoutInflater.from(mContext).inflate(R.layout.card_message_1,parent,false)
        }
        return MessageHolder(layout)
    }

    override fun getItemViewType(position: Int): Int {
        return if(listMessage[position].user_id!!.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
            1
        }else{
            2
        }

    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message=listMessage[position]
        holder.message1.text=message.message
        holder.date1.text=message.timestamp
    }

    override fun getItemCount(): Int {
        return listMessage.size
    }

}