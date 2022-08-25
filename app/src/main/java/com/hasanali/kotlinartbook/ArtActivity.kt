package com.hasanali.kotlinartbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.hasanali.kotlinartbook.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception

class ArtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val v = binding.root
        setContentView(v)

        registerLaunchers()
        setIntent()
    }

    private fun setIntent() {
        val intent = intent
        val info = intent.getStringExtra("info")
        if(info.equals("new")) {
            binding.button.visibility = View.VISIBLE
            binding.artNameText.isEnabled = true
            binding.artistNameText.isEnabled = true
            binding.artYearText.isEnabled = true
            binding.imageView.isClickable = true
        } else {
            binding.button.visibility = View.INVISIBLE
            binding.artNameText.isEnabled = false
            binding.artistNameText.isEnabled = false
            binding.artYearText.isEnabled = false
            binding.imageView.isClickable = false
            val selectedId = intent.getIntExtra("id", -1)
            try {
                val myDb = openOrCreateDatabase("Arts", MODE_PRIVATE, null)
                val cursor = myDb.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
                val artnameIx = cursor.getColumnIndex("artname")
                val artistnameIx = cursor.getColumnIndex("artistname")
                val yearIx = cursor.getColumnIndex("year")
                val imageIx = cursor.getColumnIndex("image")
                while (cursor.moveToNext()) {
                    binding.artNameText.setText(cursor.getString(artnameIx))
                    binding.artistNameText.setText(cursor.getString(artistnameIx))
                    binding.artYearText.setText(cursor.getString(yearIx))
                    val byteArray = cursor.getBlob(imageIx)
                    val bitmap = BitmapFactory.decodeByteArray(byteArray,0, byteArray.size)
                    binding.imageView.setImageBitmap(bitmap)
                }
                cursor.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveButtonClicked(v: View) {
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val artYear = binding.artYearText.text.toString()
        if(selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            try {
                val myDb = openOrCreateDatabase("Arts", MODE_PRIVATE, null)
                myDb.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)"
                val statement = myDb.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, artYear)
                statement.bindBlob(4, byteArray)
                statement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun makeSmallerBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio: Double = width.toDouble() / height.toDouble()
        if(bitmapRatio > 1) {
            // landscape
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            // portrait
            height = maxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width,height,true)
    }

    fun selectImage(v: View) {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(v, "Permission needed for gallery!", Snackbar.LENGTH_INDEFINITE).setAction("Give permission", View.OnClickListener {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)
        }
    }

    private fun registerLaunchers() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if(intentFromResult != null) {
                    val imageData = intentFromResult.data
                    if(imageData != null) {
                        try {
                            if(Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(contentResolver,imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if(result) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intent)
            } else {
                Toast.makeText(this,"Permission needed!",Toast.LENGTH_SHORT).show()
            }
        }
    }

}