package com.example.tasktune

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("DEPRECATION")
class EditProfileFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val REQUEST_IMAGE_CAPTURE = 100

    private lateinit var auth: FirebaseAuth
    private lateinit var view: View
    private lateinit var database: FirebaseDatabase

    private lateinit var emailView: TextView
    private lateinit var usernameView: TextView
    private lateinit var photoUrlView: ImageView

    private lateinit var emailVal: String
    private lateinit var usernameVal: String
    private lateinit var photoUrlVal: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        FirebaseApp.initializeApp(requireContext())
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        emailView = view.findViewById<TextView>(R.id.textEmail)
        usernameView = view.findViewById<TextView>(R.id.textUsername)
        photoUrlView = view.findViewById<ImageView>(R.id.profilePhoto)

        emailView.text = auth.currentUser?.email.toString()
        val userRef = database.getReference("users").child(auth.currentUser!!.uid)


        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val username = dataSnapshot.child("username").getValue(String::class.java)
                usernameView.text = username
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error, if necessary
            }
        })
        val storageReference = Firebase.storage.reference
        val imageRef = storageReference.child("images/${auth.currentUser?.uid.toString()}.jpg")

        if (imageRef != null) {
            println("WWWWW $imageRef")
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                photoUrlVal = uri.toString()

                // Use the imageURL to load the image using an image-loading library
                // (e.g., Glide or Picasso)
                // Example with Glide:
                Glide.with(this)
                    .load(photoUrlVal)
                    .into(photoUrlView)
            }.addOnFailureListener { exception ->
                // Handle any errors that occur while retrieving the image URL
            }
        }

//        Glide.with(this@EditProfileFragment)
//            .load(photoUrlVal)
//            .into(photoUrlView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnEditEmail = view.findViewById<ImageButton>(R.id.btnEditEmail)
        val btnEditPassword = view.findViewById<ImageButton>(R.id.btnEditPassword)
        val btnProfilePhoto = view.findViewById<Button>(R.id.btnEditProfilePhoto)

        btnEditEmail.setOnClickListener {
            val showPopUp = ChangeEmailFragment()
            showPopUp.show(parentFragmentManager, "showPopUp")
        }

        btnEditPassword.setOnClickListener() {
            val showPopUp = ChangePasswordFragment()
            showPopUp.show(parentFragmentManager, "showPopUp")
        }

        btnProfilePhoto.setOnClickListener {
            val takePictureInent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            try {
                startActivityForResult(takePictureInent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Error: Photo error has occurred" , Toast.LENGTH_LONG)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            photoUrlView.setImageBitmap(imageBitmap)

            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Uploading photo...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val bitmap: Bitmap = imageBitmap // Your bitmap image
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data: ByteArray = baos.toByteArray()

            val storageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageReference.child("images").child("${auth.currentUser?.uid.toString()}.jpg")
            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                // Image uploaded successfully
                // Retrieve the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    // Save the download URL to Firebase Realtime Database or perform any further actions
                    val userRef = FirebaseDatabase.getInstance().reference.child("users").child(auth.currentUser?.uid.toString())
                    userRef.child("photoUrl").setValue(downloadUrl)
                        .addOnSuccessListener {
                            // Download URL saved successfully
                            progressDialog.dismiss()
                            Toast.makeText(context, "Your profile picture has been successfully saved!", Toast.LENGTH_SHORT).show()

                        }
                        .addOnFailureListener { exception ->
                            // Handle download URL saving failure
                            progressDialog.dismiss()
                            Toast.makeText(context, "Something went wrong while trying to save the profile picture!", Toast.LENGTH_SHORT).show()
                        }

                }.addOnFailureListener { exception ->
                    // Handle failure to retrieve the download URL
                    progressDialog.dismiss()
                }
            }.addOnFailureListener { exception ->
                // Handle upload failure
                progressDialog.dismiss()
            }
            //val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}