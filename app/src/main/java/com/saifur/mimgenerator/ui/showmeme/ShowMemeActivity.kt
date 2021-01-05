package com.saifur.mimgenerator.ui.showmeme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.saifur.mimgenerator.R
import com.saifur.mimgenerator.data.model.memeresponse.Meme
import com.saifur.mimgenerator.databinding.ActivityShowMemeBinding
import org.koin.android.ext.android.bind
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ShowMemeActivity : AppCompatActivity() {
    private lateinit var binding:ActivityShowMemeBinding

    private var textSize = 8

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowMemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply{
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        val data = intent.getParcelableExtra<Meme>(EXTRA_DETAIL) as Meme
        loadImage(data.url)

        binding.addLogo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 300)
        }

        binding.addText.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Add Text")
            val dialogLayout = inflater.inflate(R.layout.add_text_dialog, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.et_text)
            val seekBar = dialogLayout.findViewById<SeekBar>(R.id.seekBar)
            val seekBarValue = dialogLayout.findViewById<TextView>(R.id.seekbar_value)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    seekBarValue.text = progress.toString()
                    textSize = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })

            builder.setView(dialogLayout)
            builder.setPositiveButton("Add"){ _, _ ->
                if(editText.text.toString() != ""){
                    val customText = TextView(this)
                    customText.textSize = textSize.toFloat()
                    customText.text = editText.text.toString()
                    customText.setOnLongClickListener{ v ->
                        val shadow = View.DragShadowBuilder(v)
                        v.startDrag(null, shadow, v, 0)
                        true
                    }

                    customText.setOnDragListener { _, event ->
                        Log.d("Drag", event.action.toString())
                        if(event.action == DragEvent.ACTION_DRAG_EXITED){
                            val x = event.x.toInt()
                            val y = event.y.toInt()
                            val layoutParam = RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
                            )

                            layoutParam.setMargins(x, y, 0, 0)

                            customText.layoutParams = layoutParam
                        }
                        true
                    }

                    binding.canvasLayout.addView(customText)
                }
            }

            builder.show()
        }

        binding.btnDone.setOnClickListener {
            val fileName = "meme" + System.currentTimeMillis() + ".png"
            saveImage(fileName)

            binding.layoutEdit.visibility = View.GONE
            binding.layoutDone.visibility = View.VISIBLE
        }

        binding.btnShare.setOnClickListener {
            binding.layoutDone.visibility = View.GONE
            binding.layoutShare.visibility = View.VISIBLE
        }
    }

    private fun loadImage(url: String){
        Glide.with(this)
            .load(url)
            .into(binding.imageMeme)
    }

    private fun getBitmap() : Bitmap{
        binding.canvasLayout.isDrawingCacheEnabled = true
        val bm = Bitmap.createBitmap(binding.canvasLayout.drawingCache)
        binding.canvasLayout.isDrawingCacheEnabled = false
        return bm
    }

    private fun saveImage(filename: String){
        val dirpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
        val dir = File(dirpath)
        if(!dir.exists()){
            dir.mkdir()
        }

        val bm = getBitmap()

        val file = File(dirpath, filename)
        try {
            val fs = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 100, fs)
            fs.flush()
            fs.close()
            Toast.makeText(this, "Meme Saved", Toast.LENGTH_SHORT).show()
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 300){
            val imageView = ImageView(this)
            imageView.setImageURI(data?.data)
            binding.canvasLayout.addView(imageView)
        }
    }

    companion object{
        const val EXTRA_DETAIL = "extra_detail"
    }

}