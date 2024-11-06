package com.daniel.cineverse

import android.os.Parcel
import android.os.Parcelable
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

data class Movie(val name: String, val imageResId: Int, var comment: String? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(imageResId)
        parcel.writeString(comment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Movie> {
        override fun createFromParcel(parcel: Parcel): Movie {
            return Movie(parcel)
        }

        override fun newArray(size: Int): Array<Movie?> {
            return arrayOfNulls(size)
        }
    }

    fun saveToFirebase(packageName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference

        val imageUri = Uri.parse("android.resource://$packageName/$imageResId")

        val imageRef = storageRef.child("movies/$name.jpg")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val movieData = hashMapOf(
                    "name" to name,
                    "imageUrl" to uri.toString(),
                    "comment" to comment
                )

                db.collection("movies").add(movieData)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }
}
