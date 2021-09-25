package com.example.sohbetappkotlinfirebase.Activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sohbetappkotlinfirebase.ClassModel.User
import com.example.sohbetappkotlinfirebase.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.ByteArrayOutputStream

class UserProfileActivity : AppCompatActivity() {
    var permissionStatus = false
    var galeryUri: Uri? = null
    var cameraBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        val user = FirebaseAuth.getInstance().currentUser
        profilInfoWrite(user)
        buttonProfilPassReset.setOnClickListener {
            val mail = editUserProfilMail.text.toString().trim()
            if (mail.isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "Sıfırlama maili gömderildi",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Hata: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(applicationContext, "Mail alanı boş olamaz", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        buttonProfilUpdate.setOnClickListener {
            val name = editUserProfilName.text.toString().trim()
            val myRef = FirebaseDatabase.getInstance().getReference("users")
            if (name.isNotEmpty()) {
                val userInfoUpdate = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                user?.updateProfile(userInfoUpdate)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                            .child("name").setValue(name)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Bilgiler güncellendi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Hata: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(applicationContext, "Alanlar boş olamaz", Toast.LENGTH_SHORT).show()
            }

            val tel = editUserTel.text.toString().trim()
            if (tel.isNotEmpty()) {
                myRef.child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("telephone").setValue(tel)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "Bilgiler güncellendi",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Hata: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(applicationContext, "Alanları doldurunuz", Toast.LENGTH_SHORT).show()
            }

            if (galeryUri != null) {
                photoCompressed(galeryUri!!)
            }

            if (cameraBitmap != null) {
                photoCompressed(cameraBitmap!!)
            }
        }

        imageViewUserProfil.setOnClickListener {
            if (permissionStatus) {
                alertOpen()
            } else {
                askPermission()
            }
        }
    }

    private fun photoCompressed(galeryUri: Uri) {
        val compress = BackgroundImageCompress()
        compress.execute(galeryUri)
    }

    private fun photoCompressed(birmap: Bitmap) {
        val compress = BackgroundImageCompress(birmap)
        val uri: Uri? = null
        compress.execute(uri)
    }

    private fun askPermission() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        )

        if (ContextCompat.checkSelfPermission(
                this@UserProfileActivity,
                permissions[0]
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this@UserProfileActivity,
                permissions[1]
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this@UserProfileActivity,
                permissions[2]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionStatus = true
        } else {
            ActivityCompat.requestPermissions(this@UserProfileActivity, permissions, 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                alertOpen()
            } else {
                Toast.makeText(applicationContext, "Tüm izinleri verin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun profilInfoWrite(user: FirebaseUser?) {
        progressUserprofil.visibility=View.VISIBLE
        val myRef = FirebaseDatabase.getInstance().getReference("users")
        val query = myRef.orderByKey().equalTo(user?.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    val user1 = i.getValue(User::class.java)
                    if (user1 != null) {
                        editUserProfilName.setText(user1.name)
                        editUserProfilMail.setText(user?.email)
                        editUserTel.setText(user1.telephone)
                        if (user1.profile_image.isNullOrEmpty()){
                            imageViewUserProfil.setImageResource(R.drawable.ic_baseline_account_circle_24)
                        }else{
                            Picasso.get().load(user1.profile_image).resize(100, 150).into(imageViewUserProfil);
                        }
                        progressUserprofil.visibility=View.INVISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun alertOpen() {
        val alertView = layoutInflater.inflate(R.layout.alert_photo, null)
        val textGalery = alertView.findViewById(R.id.textViewAlertgalery) as TextView
        val textCamera = alertView.findViewById(R.id.textViewAlertCamera) as TextView

        val ad = AlertDialog.Builder(this@UserProfileActivity)
        ad.setTitle("Profil fotoğrafı")
        ad.setMessage("Resim seçiniz")
        ad.setView(alertView)

        textGalery.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 200)
        }

        textCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 300)
        }
        ad.setNegativeButton("İptal") { _, _ ->

        }

        ad.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            galeryUri = data.data
            Picasso.get().load(galeryUri).resize(100, 150).into(imageViewUserProfil);
        }

        if (requestCode == 300 && resultCode == Activity.RESULT_OK && data != null) {
            cameraBitmap = data.extras!!.get("data") as Bitmap
            imageViewUserProfil.setImageBitmap(cameraBitmap)
        }
    }

    inner class BackgroundImageCompress : AsyncTask<Uri, Void, ByteArray?> {
        var myBitmap: Bitmap? = null

        constructor()
        constructor(bitmap: Bitmap) {
            myBitmap = bitmap
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)

        }

        override fun doInBackground(vararg p0: Uri?): ByteArray? {
            if (myBitmap == null) {
                myBitmap = MediaStore.Images.Media.getBitmap(
                    this@UserProfileActivity.contentResolver,
                    p0[0]
                )
            }

            var photoByte: ByteArray? = null

            for (i in 1..8) {
                photoByte = convertBitmaptoByte(myBitmap, 100 / i)
            }

            return photoByte
        }

        private fun convertBitmaptoByte(myBitmap: Bitmap?, i: Int): ByteArray? {
            val stream = ByteArrayOutputStream()
            myBitmap?.compress(Bitmap.CompressFormat.JPEG, i, stream)
            return stream.toByteArray()
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
            uploadPhotoFirebase(result)
        }
    }

    private fun uploadPhotoFirebase(result: ByteArray?) {
        progressBarİmage.visibility=View.VISIBLE
        val storageReference = FirebaseStorage.getInstance().reference
        val imageLocation = storageReference.child("image/users${FirebaseAuth.getInstance().currentUser?.uid}/profie_photo")
        val imageUpload=imageLocation.putBytes(result!!)
        imageUpload.addOnSuccessListener { p0 ->
            val photoURl=p0.metadata?.reference?.downloadUrl
            photoURl!!.addOnCompleteListener {task->
                val link=task.result
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("profile_image").setValue(link.toString()).addOnCompleteListener { task->
                        if (task.isSuccessful){
                            Toast.makeText(applicationContext,"Fotoğraf güncellendi",Toast.LENGTH_SHORT).show()
                            progressBarİmage.visibility=View.INVISIBLE
                        }else{
                            Toast.makeText(applicationContext,"Hata: ${task.exception?.message}",Toast.LENGTH_SHORT).show()
                            progressBarİmage.visibility=View.INVISIBLE
                        }
                    }
            }
        }
    }
}