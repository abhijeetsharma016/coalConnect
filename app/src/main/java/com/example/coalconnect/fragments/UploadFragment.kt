package com.example.coalconnect.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.coalconnect.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UploadFragment : Fragment() {

    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var photoUri: Uri? = null
    private var photoFile: File? = null
    private lateinit var imageUpload: ImageView
    private lateinit var editTextDesc: EditText
    private lateinit var datetime: TextView
    private lateinit var progressDialog: ProgressDialog

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                photoUri?.let { Glide.with(this).load(it).into(imageUpload) }
            } else {
                Toast.makeText(context, "Please capture an image", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        storageReference = FirebaseStorage.getInstance().reference.child("images")
        databaseReference = FirebaseDatabase.getInstance().reference.child("events")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        imageUpload = view.findViewById(R.id.ImageUpload)
        editTextDesc = view.findViewById(R.id.edittextUplDesc)
        datetime = view.findViewById(R.id.datetime)
        val imageAddButton: FloatingActionButton = view.findViewById(R.id.imageaddbutton)
        val uploadButton: TextView = view.findViewById(R.id.uploadbutton2)

        progressDialog = ProgressDialog(context)

        val currentDatetime = DateFormat.format("yyyy-MM-dd HH:mm:ss", Date()).toString()
        datetime.text = "Current time $currentDatetime"

        imageAddButton.setOnClickListener {
            try {
                dispatchTakePictureIntent()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        uploadButton.setOnClickListener {
            if (photoUri != null) {
                uploadDataToFirebase()
            } else {
                Toast.makeText(context, "No image captured", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            photoFile = createImageFile()
            photoFile?.let {
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.coalconnect.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                activityResultLauncher.launch(takePictureIntent)
            }
        }
    }


    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun uploadDataToFirebase() {
        val description = editTextDesc.text.toString().trim()
        val currentDatetime = DateFormat.format("yyyy-MM-dd HH:mm:ss", Date()).toString()

        if (description.isEmpty()) {
            Toast.makeText(context, "Please fill the description", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.setMessage("Uploading...")
        progressDialog.show()

        val fileRef = storageReference.child("${UUID.randomUUID()}.jpg")
        fileRef.putFile(photoUri!!)
            .addOnSuccessListener { taskSnapshot ->
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    val event = hashMapOf(
                        "description" to description,
                        "datetime" to currentDatetime,
                        "imageUrl" to imageUrl
                    )

                    databaseReference.push().setValue(event)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Data uploaded successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
