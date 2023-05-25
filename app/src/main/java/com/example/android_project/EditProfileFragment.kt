package com.example.android_project

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
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
    private lateinit var database: DatabaseReference

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val REQUEST_IMAGE_CAPTURE = 100

    private lateinit var auth: FirebaseAuth
    private var email: String? = null
    private lateinit var uid: String
    private lateinit var usernameVal: String
    private lateinit var photoUrlVal: String

    private lateinit var imageView: ImageView
    private lateinit var btnProfilePhoto: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val user = FirebaseAuth.getInstance().currentUser
        email = user?.email.toString()
        uid = user?.uid.toString()
        val userTest = UserDataManager.getUser()
        if (userTest != null) {
            // Use the user data as needed
            usernameVal = userTest.username.toString()
            photoUrlVal = userTest.photoUrl.toString()
//            val userName = userTest.username
//            val userEmail = userTest.email
//            val userId = userTest.uid
//            Log.e(tag, "AAAA "+ userName)
//            Log.e(tag, "AAAA "+ userEmail)
//            Log.e(tag, "AAAA "+ userId)
            Log.e(tag, "AAAA "+ photoUrlVal)
            // ...
        } else {
            // Handle the case where user data is not available
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        val textEmail = view.findViewById<TextView>(R.id.textEmail)
        val textUsername = view.findViewById<TextView>(R.id.textUsername)
        val profilePhoto = view.findViewById<ImageView>(R.id.profilePhoto)
        textEmail.text = email
        textUsername.text = usernameVal
        Glide.with(this@EditProfileFragment)
            .load(photoUrlVal)
            .into(profilePhoto)

        return view
//        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnEditEmail = view.findViewById<ImageButton>(R.id.btnEditEmail)
        val btnEditPassword = view.findViewById<ImageButton>(R.id.btnEditPassword)
        imageView = view.findViewById(R.id.profilePhoto)
        btnProfilePhoto = view.findViewById<Button>(R.id.btnEditProfilePhoto)

        btnEditEmail.setOnClickListener {
            val showPopUp = PopUpFragment()
            showPopUp.show(parentFragmentManager, "showPopUp")
        }

        btnEditPassword.setOnClickListener {
            val showPopUp = PopUpEditPassFragment()
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)

            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Uploading photo...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val bitmap: Bitmap = imageBitmap // Your bitmap image
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data: ByteArray = baos.toByteArray()

            val storageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageReference.child("images").child("$uid.jpg")
            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                // Image uploaded successfully
                // Retrieve the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    // Save the download URL to Firebase Realtime Database or perform any further actions
                    val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
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