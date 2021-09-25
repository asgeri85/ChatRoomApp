package com.example.sohbetappkotlinfirebase.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.sohbetappkotlinfirebase.ClassModel.User
import com.example.sohbetappkotlinfirebase.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth=FirebaseAuth.getInstance()
        buttonRegister.setOnClickListener {
            val email=editRegisterMail.text.toString().trim()
            val password=editRegisterPassword.text.toString().trim()
            val pass2=editRegisterPassword2.text.toString().trim()
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(pass2)){
                if (password.equals(pass2)){
                    registerUser(email,password)
                }else{
                    Toast.makeText(applicationContext,"Şifreler aynı değil",Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(applicationContext,"Tüm alanları doldurunuz",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registerUser(name:String, pass:String){
        progressBarRegister.visibility=View.VISIBLE
        mAuth.createUserWithEmailAndPassword(name,pass).addOnCompleteListener { p0 ->
            if (p0.isSuccessful) {
                sendMailVerification()
                val user = User(name.substring(0,name.indexOf("@")), FirebaseAuth.getInstance().currentUser?.uid, "000", "", "1")
                val myRef = FirebaseDatabase.getInstance().getReference("users")
                    myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).setValue(user).addOnCompleteListener { task->
                        if(task.isSuccessful){
                            Toast.makeText(applicationContext,"Uye kaydedildi: ${FirebaseAuth.getInstance().currentUser?.email}",Toast.LENGTH_SHORT)
                                .show()
                            FirebaseAuth.getInstance().signOut()
                            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                        }
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Kayıt hatalı: ${p0.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        progressBarRegister.visibility=View.INVISIBLE
    }

    fun sendMailVerification(){
        val user=FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { p0 ->
            if (p0.isSuccessful) {
                Toast.makeText(applicationContext, "Onay maili gonderildi", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}