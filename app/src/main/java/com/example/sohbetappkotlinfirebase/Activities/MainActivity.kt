package com.example.sohbetappkotlinfirebase.Activities

import  android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.sohbetappkotlinfirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mAutStatehListener: FirebaseAuth.AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAuthListener()
        tokenRefres()
    }

    private fun tokenRefres() {
        val token=FirebaseMessaging.getInstance().token.addOnCompleteListener {task->
            if(task.isSuccessful){
                val messageToken=task.result
                val ref=FirebaseDatabase.getInstance().getReference("users")
                val addToken=ref.child(FirebaseAuth.getInstance().currentUser!!.uid).child("token").setValue(messageToken)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_exit->{
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@MainActivity,LoginActivity::class.java))
                finish()
                true
            }

            R.id.action_profile->{
                startActivity(Intent(this@MainActivity,UserProfileActivity::class.java))
                true
            }

            R.id.action_chatRoom->{
                startActivity(Intent(this@MainActivity,ChatActivity::class.java))
                true
            }
            else-> false
        }
    }

    private fun initAuthListener(){
        mAutStatehListener= FirebaseAuth.AuthStateListener { p0 ->
            val user=p0.currentUser
            if (user == null){
                val intent=Intent(this@MainActivity,LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo(){
        val user=FirebaseAuth.getInstance().currentUser
        if (user?.displayName.isNullOrEmpty()){
            textViewHomeUsername.text="Tanımlanmadı"
        }else{
            textViewHomeUsername.text=user?.displayName
        }
        textViewHomeMail.text=user?.email
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAutStatehListener)
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAutStatehListener)
    }

    override fun onResume() {
        super.onResume()
        userInfo()
        val user=FirebaseAuth.getInstance().currentUser
        if (user == null){
            val intent=Intent(this@MainActivity,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}