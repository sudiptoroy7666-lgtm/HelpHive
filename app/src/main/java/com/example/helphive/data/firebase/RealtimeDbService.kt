package com.example.helphive.data.firebase

import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject

class RealtimeDbService @Inject constructor(private val database: FirebaseDatabase) {

    fun uploadImage(imagePath: String, base64String: String) {
        val ref = database.getReference("images/$imagePath")
        ref.setValue(base64String)
    }

    fun getImage(imagePath: String, callback: (String?) -> Unit) {
        val ref = database.getReference("images/$imagePath")
        ref.get().addOnSuccessListener { snapshot ->
            callback(snapshot.value as? String)
        }.addOnFailureListener {
            callback(null)
        }
    }


    fun observeImage(imagePath: String, callback: (String?) -> Unit) {
        val ref = database.getReference("images/$imagePath")
        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                callback(snapshot.value as? String)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                callback(null)
            }
        })
    }
}