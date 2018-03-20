package com.anikethan.android.barcodescanner



import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*
import org.bouncycastle.util.encoders.Base64
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val LOG_TAG = "Barcode Scanner API"
    private val PHOTO_REQUEST = 10
    private var scanResults: TextView? = null
    private var decode: TextView? = null
    private var detector: BarcodeDetector? = null
    private var imageUri: Uri? = null
    private val REQUEST_WRITE_PERMISSION = 20
    private val SAVED_INSTANCE_URI = "uri"
    private val SAVED_INSTANCE_RESULT = "result"
    private var currImagePath: String? = null
    internal var imageFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val button = findViewById<View>(R.id.button) as Button
        scanResults = findViewById<View>(R.id.scan_results) as TextView
        decode = findViewById<View>(R.id.decode) as TextView
        if (savedInstanceState != null) {
            imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI))
            scanResults!!.text = savedInstanceState.getString(SAVED_INSTANCE_RESULT)
        }

//        button.setOnClickListener { ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION) }

        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
        detector = BarcodeDetector.Builder(applicationContext)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()
        if (!detector!!.isOperational) {
            scanResults!!.text = "Could not set up the detector!"
            return
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_PERMISSION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture()
            } else {
                Toast.makeText(this@MainActivity, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("HH== requestCode +"+requestCode)
        println("HH==  resultCode "+resultCode)
        println("HH== data "+data)
        println("HH== RESULT_OK " + Activity.RESULT_OK)

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {

            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = imageUri
            println("HH== imageUri " + imageUri!!)
            launchMediaScanIntent(mediaScanIntent)
            try {
                val bitmap = decodeBitmapUri(this, imageUri)
                println("HH== bitmap  " + bitmap)
                if (detector!!.isOperational && bitmap != null) {
                    val frame = Frame.Builder().setBitmap(bitmap).build()
                    val barcodes = detector!!.detect(frame)
                    println("HH== barcodes "+barcodes)
                    for (index in 0 until barcodes.size()) {
                        val code = barcodes.valueAt(index)
                        scanResults!!.text = scanResults!!.text.toString() + code.displayValue

//                        val response = code.displayValue + "\n"
//                        val TaResponse = Base64.decode(response)
//                        println("Decoded value of TA " + String(TaResponse))

//                        val TaResponse = ByteArray(
//                                val TaResponse : ByteArray
//                        TaResponse = Base64.decode(response)
//val response = "<QPDA n=\"Priyanka Prakash Vaze\" u=\"xxxxxxxx2617\" g=\"F\" d=\"11-04-1992\" a=\"SAFFIRE-3 EMROLD CITY GARKHEDA PARISAR,Garkheda S.O,Aurangabad,Maharashtra,431009\" i=\"AAAADGpQICANCocKAAAAFGZ0eXBqcDIgAAAAAGpwMiAAAAAtanAyaAAAABZpaGRyAAAAyAAAAKAAAwcHAAAAAAAPY29scgEAAAAAABAAAAG6anAyY/9P/1EALwAAAAAAoAAAAMgAAAAAAAAAAAAAAKAAAADIAAAAAAAAAAAAAwcBAQcBAQcBAf9SAAwAAAABAQUEBAAA/1wAI0JvGG7qbupuvGcAZwBm4l9MX0xfZEgDSANIRU/ST9JPYf9kACIAAUNyZWF0ZWQgYnk6IEpKMjAwMCB2ZXJzaW9uIDQuMf+QAAoAAAAAASYAAf9SAAwAAAABAQUEBAAA/5PH0GQ1SwAoHtjwXrBdOfrdyjbKPfme78ftSPmiwfEOHYZ81Z2JKJTnKGIs2CjB8Q0eMoLKcwpq5vL5HGpdx9BiHyDYPhLAKNV0/qkVqAItSLWv5ZjeFV9yEDjeIVOfMbVWjblNhd5dS4XygiA/Wv6yOQ8v1v4QwEIAYoDD4lUHU4OlgB+sUKe8zAaq9h8pDCSnhWHAszvK0HFMAXVGEJDKLEnn5cjdeKadegoE+ijeZuk9MxD8elsSe3F9oxHv7LKJkrWyj6TW1ufemYCAw6pDq8EgpjRW5/c/oNDHXG8INZBezt1CJJyigT+CQ4B0ZKhQ7yPlkbbaBEbOCMH+A4N0hmaAgICAgICAgP/Z\" x=\"\" s=\"pAgWZLTBCC/5BYLu5AMG3u/8EmqfUMSoQyLjk7ma8XYtxpjdC4g/xmaove+NmbkDVimX8TgU1XjIOsZeJQo0lykX6B1SXhu9+aY55IK+C4y9W4uZx5Uf0pRr92j+5K1ZzNS4SuEwiHNLPgOyaOwmUlMYdFMRKLlLWSwrunJyNngDVGewVMLofh3Muss6UnImSwj95t4hqsCl0VYDR5VAYx6hN/hr4DAIWDZNG2JVGZudjOl9RQ7DLh3O7uSuwISsmoz1KHhKCfr1MSAOjGH1TRzIO7L5tsQHkjigsGy1YFQdFzUwrhkLuht34gJ7xbwV3lgOwg0wiIZ993n55xhVpA==\"/>"
//                        val TaResponse = Base64.decode(response)
//                        decode!!.text = decode!!.text.toString() +String(TaResponse)
//                        println("HH== Decoded value of TA " + String(TaResponse))
//                        byte[] TaResponse = Base64.decode(response);
//                        System.out.println("Decoded value of TA " + new String(TaResponse));
                        //Required only if you need to extract the type of barcode
                        val type = barcodes.valueAt(index).valueFormat
                        when (type) {
                            Barcode.CONTACT_INFO -> Log.i(LOG_TAG, code.contactInfo.title)
                            Barcode.EMAIL -> Log.i(LOG_TAG, code.email.address)
                            Barcode.ISBN -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.PHONE -> Log.i(LOG_TAG, code.phone.number)
                            Barcode.PRODUCT -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.SMS -> Log.i(LOG_TAG, code.sms.message)
                            Barcode.TEXT -> Log.i(LOG_TAG, code.rawValue)
                            Barcode.URL -> Log.i(LOG_TAG, "url: " + code.url.url)
                            Barcode.WIFI -> Log.i(LOG_TAG, code.wifi.ssid)
                            Barcode.GEO -> Log.i(LOG_TAG, code.geoPoint.lat.toString() + ":" + code.geoPoint.lng)
                            Barcode.CALENDAR_EVENT -> Log.i(LOG_TAG, code.calendarEvent.description)
                            Barcode.DRIVER_LICENSE -> Log.i(LOG_TAG, code.driverLicense.licenseNumber)
                            else -> Log.i(LOG_TAG, code.rawValue)
                        }
                    }
                    if (barcodes.size() == 0) {
                        scanResults!!.text = "Scan Failed: Found nothing to scan"
                    }
                } else {
                    scanResults!!.text = "Could not set up the detector!"
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                        .show()
                Log.e(LOG_TAG, e.toString())
            }

        }
    }

    private fun takePicture() {
        //        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        //        imageUri = FileProvider.getUriForFile(A.this,
        //                BuildConfig.APPLICATION_ID + ".provider", photo);
        //        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //        System.out.println("HH== intent "+intent);
        //        System.out.println("HH== PHOTO_REQUEST "+PHOTO_REQUEST);

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            imageFile = createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        imageUri = Uri.fromFile(imageFile)


        println("HH== intent "+intent)
        println("HH== PHOTO_REQUEST "+PHOTO_REQUEST)
        println("HH== imageUri  " + imageUri!!)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, PHOTO_REQUEST)
        } else {
            println("HH== No Start Activity")
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = File(Environment.getExternalStorageDirectory(), "picture.jpg")
        if (!storageDir.exists()) {
            storageDir.parentFile.mkdirs()
            storageDir.createNewFile()
        }
        currImagePath = storageDir.absolutePath
        println("HH== currImagePath. " + currImagePath!!)
        return storageDir
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (imageUri != null) {
            outState!!.putString(SAVED_INSTANCE_URI, imageUri!!.toString())
            outState.putString(SAVED_INSTANCE_RESULT, scanResults!!.text.toString())
            println("HH== scanResults " + scanResults!!.text.toString())
            println("HH== imageUri  " + imageUri!!)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(savedInstanceState)
        println("HH== onRestoreInstanceState imageUri " + savedInstanceState.getParcelable("SAVED_INSTANCE_URI")!!)
    }

    private fun launchMediaScanIntent(mediaScanIntent: Intent) {

        this.sendBroadcast(mediaScanIntent)
    }

    @Throws(FileNotFoundException::class)
    private fun decodeBitmapUri(ctx: Context, uri: Uri?): Bitmap? {
        println("HH== uri  " + uri!!)
        println("HH== ctx  $ctx")
        val targetW = 600
        val targetH = 600
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        println("HH== bmOptions  "+bmOptions)
        BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(uri), null, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        println("HH== photoW  +"+photoW)
        println("HH==  photoH +"+photoH)

        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)
        println("HH== scaleFactor +"+scaleFactor)
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeStream(ctx.contentResolver
                .openInputStream(uri), null, bmOptions)
    }



}
