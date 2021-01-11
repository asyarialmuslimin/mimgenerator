package com.saifur.mimgenerator.ui.showmeme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.saifur.mimgenerator.R
import com.saifur.mimgenerator.data.model.memeresponse.Meme
import com.saifur.mimgenerator.databinding.ActivityShowMemeBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ShowMemeActivity : AppCompatActivity() {
    private lateinit var binding:ActivityShowMemeBinding

    private var mIsScrolling = false

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
            val whiteButton = dialogLayout.findViewById<ImageButton>(R.id.btn_white)
            val whiteDrawable = whiteButton.background as GradientDrawable
            val blackButton = dialogLayout.findViewById<ImageButton>(R.id.btn_black)
            val blackDrawable = blackButton.background as GradientDrawable
            var textColor = Color.BLACK

            editText.textSize = 20f
            seekBar.progress = 20

            whiteButton.setOnClickListener {
                whiteDrawable.setStroke(2, Color.YELLOW)
                blackDrawable.setStroke(2, Color.BLACK)
                textColor = Color.WHITE
                editText.setBackgroundColor(Color.BLACK)
                editText.setTextColor(Color.WHITE)
            }

            blackButton.setOnClickListener {
                whiteDrawable.setStroke(2, Color.BLACK)
                blackDrawable.setStroke(2, Color.YELLOW)
                textColor = Color.BLACK
                editText.setBackgroundColor(Color.WHITE)
                editText.setTextColor(Color.BLACK)
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    editText.textSize = progress.toFloat()
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
                    customText.setTextColor(textColor)
                    val container = RelativeLayout(this)
                    customText.setOnTouchListener(MoveViewTouchListener(container))
                    container.addView(customText)

                    binding.canvasLayout.addView(container)
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 300){

            val imageView = ImageView(this)
            imageView.setImageURI(data?.data)
            val container = RelativeLayout(this)
            imageView.setOnTouchListener(MoveViewTouchListener(container))
            container.addView(imageView)

            binding.canvasLayout.addView(container)

        }
    }

    companion object{
        const val EXTRA_DETAIL = "extra_detail"
    }

    inner class MoveViewTouchListener(iView: View) : View.OnTouchListener {
        private val mView:View = iView
        private val mGestureListener: GestureDetector.OnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            private var mMotionDownX = 0f
            private var mMotionDownY = 0f
            override fun onDown(e: MotionEvent): Boolean {
                mMotionDownX = e.rawX - mView.translationX
                mMotionDownY = e.rawY - mView.translationY
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                mIsScrolling = true
//                trash.setVisibility(View.VISIBLE)
                mView.translationX = e2.rawX - mMotionDownX
                mView.translationY = e2.rawY - mMotionDownY
                return true
            }
        }

        private var mGestureDetector: GestureDetector = GestureDetector(iView.context, mGestureListener)

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event?.action == MotionEvent.ACTION_UP){
                mIsScrolling = false
            }

            if(mGestureDetector.onTouchEvent(event)){
                return true
            }

            if(event?.action == MotionEvent.ACTION_UP){
                if(mIsScrolling){
                    mIsScrolling = false
                }
            }
            return false
        }
    }

}