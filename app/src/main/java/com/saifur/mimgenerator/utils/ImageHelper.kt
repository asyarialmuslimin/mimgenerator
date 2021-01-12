package com.saifur.mimgenerator.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.facebook.FacebookSdk
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


object ImageHelper {
    fun saveMediaToStorage(context: Context, bitmap: Bitmap) {
        val filename = "Meme-${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {

                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
                Toast.makeText(context, "Saved Image to $imageUri", Toast.LENGTH_SHORT).show()
            }
        } else {
            val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
            Toast.makeText(context, "Saved Image to ${image.absolutePath}", Toast.LENGTH_SHORT).show()
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    fun shareImage(activity: Activity, bitmap: Bitmap, method: SharingMethod){
        val context = activity.baseContext
        val filename = "ShareTemp.png"

        var tempImage:File? = null

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {

                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fos = imageUri?.let { resolver.openOutputStream(it) }
                tempImage = File(imageUri.toString(), filename)
            }
        } else {
            val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
            tempImage = image
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if(method == SharingMethod.TWITTER){
            try {
                val builder = VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())

                val tweetIntent = Intent(Intent.ACTION_SEND)
                tweetIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempImage))
                tweetIntent.type = "image/png"

                val packManager: PackageManager = context.packageManager
                val resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_ALL)

                var resolved = false
                for (ri in resolvedInfoList) {
                    Log.d("IntentActivity", ri.activityInfo.name)
                    if (ri.activityInfo.name.contains("twitter")) {
                        tweetIntent.setClassName(ri.activityInfo.packageName,
                                ri.activityInfo.name);
                        resolved = true;
                        break;
                    }

                }

                if(resolved){
                    context.startActivity(tweetIntent)
                }else{
                    Toast.makeText(context, "No Twitter Found", Toast.LENGTH_SHORT).show()
                }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }else if(method == SharingMethod.FACEBOOK){

            FacebookSdk.setApplicationId("1114219055686393")
            FacebookSdk.sdkInitialize(context)

            val photo = SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build()
            val content = SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build()

            ShareDialog.show(activity, content)
        }

    }
}