package com.example.sohbetappkotlinfirebase.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sohbetappkotlinfirebase.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
   private lateinit var mAutStatehListener: FirebaseAuth.AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initMyAuthStateListener()

        buttonLogin.setOnClickListener {
            val name = editLoginName.text.toString().trim()
            val password = editLoginPass.text.toString().trim()
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                login(name, password)
            } else {
                Toast.makeText(applicationContext, "Tüm alanları doldurunuz", Toast.LENGTH_LONG)
                    .show()
            }
        }

        textViewRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        textViewPassReset.setOnClickListener {
            alertOpen()
        }
    }

    private fun login(mail: String, pass: String) {
        progressBarLogin.visibility = View.VISIBLE
        FirebaseAuth.getInstance().signInWithEmailAndPassword(mail, pass)
            .addOnCompleteListener { p0 ->
                if (p0.isSuccessful) {
                    if(!p0.result?.user?.isEmailVerified!!){
                        FirebaseAuth.getInstance().signOut()
                    }
                    progressBarLogin.visibility = View.INVISIBLE
                } else {
                    progressBarLogin.visibility = View.INVISIBLE
                    Toast.makeText(
                        applicationContext,
                        "Hatalı giriş: ${p0.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun initMyAuthStateListener() {
        mAutStatehListener= FirebaseAuth.AuthStateListener { p0 ->
            val kullanici=p0.currentUser

            if (kullanici!=null){
                if (kullanici.isEmailVerified){
                    startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(applicationContext,"Hesabınızı onaylayın",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun alertOpen(){
        val viewAlert=layoutInflater.inflate(R.layout.reset_password_alert,null)
        val editResetPass=viewAlert.findViewById(R.id.editResetPass) as EditText
        val ad=AlertDialog.Builder(this@LoginActivity)
        ad.setTitle("Şifre sıfırlama")
        ad.setMessage("Mail adresinizi giriniz")
        ad.setView(viewAlert)

        ad.setPositiveButton("Gönder"){ inderface,i->
            val mail=editResetPass.text.toString().trim()
            if (!TextUtils.isEmpty(mail)){
                FirebaseAuth.getInstance().sendPasswordResetEmail(mail).addOnCompleteListener { task->
                    if (task.isSuccessful){
                        Toast.makeText(applicationContext,"Mail gonderildi",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(applicationContext,"Hata: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        ad.create().show()
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAutStatehListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAutStatehListener)
    }

}