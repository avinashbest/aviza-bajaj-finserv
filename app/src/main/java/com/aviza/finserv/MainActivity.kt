package com.aviza.finserv

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aviza.docscanner.ImageCropActivity
import com.aviza.docscanner.helpers.ScannerConstants
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var btnPick: Button
    private lateinit var imgBitmap: ImageView
    private lateinit var mCurrentPhotoPath: String

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1111 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            var btimap: Bitmap? = null
            try {
                val inputStream = selectedImage?.let { contentResolver.openInputStream(it) }
                btimap = BitmapFactory.decodeStream(inputStream)
                ScannerConstants.selectedImageBitmap = btimap
                startActivityForResult(
                    Intent(this, ImageCropActivity::class.java),
                    1234
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (requestCode == 1231 && resultCode == Activity.RESULT_OK) {
            ScannerConstants.selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                Uri.parse(mCurrentPhotoPath)
            )
            startActivityForResult(Intent(this, ImageCropActivity::class.java), 1234)
        } else if (requestCode == 1234 && resultCode == Activity.RESULT_OK) {
            if (ScannerConstants.selectedImageBitmap != null) {
                imgBitmap.setImageBitmap(ScannerConstants.selectedImageBitmap)
                imgBitmap.visibility = View.VISIBLE
                btnPick.visibility = View.GONE
            } else {
                Toast.makeText(this, "Not OK", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnPick = findViewById(R.id.btnPick)
        imgBitmap = findViewById(R.id.imgBitmap)
        askPermission()
    }

    private fun askPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ), 100
            )
        }
        setView()
    }

    private fun setView() {
        btnPick.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Document")
            builder.setMessage("Where would you like to choose the image ?")
            builder.setPositiveButton("Gallery") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 1111)
            }
            builder.setNegativeButton("Camera") { dialog, _ ->
                dialog.dismiss()
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                    } catch (ex: IOException) {
                        Log.i("Main", "IOException")
                    }
                    if (photoFile != null) {
                        val builder = StrictMode.VmPolicy.Builder()
                        StrictMode.setVmPolicy(builder.build())
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                        startActivityForResult(cameraIntent, 1231)
                    }
                }
            }
            builder.setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        val image = File.createTempFile(
            imageFileName, // prefix
            ".jpg", // suffix
            storageDir      // directory
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }

}