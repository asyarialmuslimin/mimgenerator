package com.saifur.mimgenerator.ui.showmeme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.saifur.mimgenerator.R
import com.saifur.mimgenerator.component.MultiTouchGestureView
import com.saifur.mimgenerator.data.model.memeresponse.Meme
import com.saifur.mimgenerator.databinding.ActivityShowMemeBinding
import com.saifur.mimgenerator.utils.ImageHelper
import com.saifur.mimgenerator.utils.SharingMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

class ShowMemeActivity : AppCompatActivity() {
    private lateinit var binding:ActivityShowMemeBinding

    private var mIsScrolling = false

    private var textSize = 20

    private val outRect = Rect()
    private val location = IntArray(2)

    var elementCount = 0

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

                    customText.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

                    customText.layout(0, 0, customText.measuredWidth, customText.measuredHeight)
                    customText.isDrawingCacheEnabled = true
                    customText.buildDrawingCache()

                    val drwText = BitmapDrawable(resources, customText.drawingCache)
                    val drwContainer = MultiTouchGestureView(this, null, drwText)

                    val container = RelativeLayout(this)
                    drwContainer.setOnTouchListener(MoveViewTouchListener(container))
                    container.addView(drwContainer)

                    binding.canvasLayout.addView(container)
                    elementCount++
                }
            }

            builder.show()
        }

        binding.btnDone.setOnClickListener {
            binding.layoutEdit.visibility = View.GONE
            binding.layoutDone.visibility = View.VISIBLE
        }

        binding.btnShare.setOnClickListener {
            binding.layoutDone.visibility = View.GONE
            binding.layoutShare.visibility = View.VISIBLE
        }

        binding.btnSimpan.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                ImageHelper.saveMediaToStorage(this@ShowMemeActivity, getScreenshot())
            }
        }

        binding.btnTwitterShare.setOnClickListener {
            ImageHelper.shareImage(this, getScreenshot(), SharingMethod.TWITTER)
        }

        binding.btnFbShare.setOnClickListener {
            ImageHelper.shareImage(this, getScreenshot(), SharingMethod.FACEBOOK)
        }
    }

    private fun loadImage(url: String){
        Glide.with(this)
            .load(url)
            .fitCenter()
            .into(binding.imageMeme)
    }

    private fun getScreenshot() : Bitmap{
        binding.canvasLayout.isDrawingCacheEnabled = true
        val bm = Bitmap.createBitmap(binding.canvasLayout.drawingCache)
        binding.canvasLayout.isDrawingCacheEnabled = false
        return bm
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 300){

            val imageUri = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            val bmDraw = BitmapDrawable(resources, bitmap)

            val imageView = MultiTouchGestureView(this, null, bmDraw)

            val container = RelativeLayout(this)
            imageView.setOnTouchListener(MoveViewTouchListener(container))
            container.addView(imageView)

            binding.canvasLayout.addView(container)
            elementCount++
        }
    }

    companion object{
        const val EXTRA_DETAIL = "extra_detail"
    }

    inner class MoveViewTouchListener(iView: View) : View.OnTouchListener {
        private val mGestureListener: GestureDetector.OnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                mIsScrolling = true
                binding.trash.visibility = View.VISIBLE
                binding.layoutEdit.visibility = View.GONE
                return true
            }
        }

        private var mGestureDetector: GestureDetector = GestureDetector(iView.context, mGestureListener)

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event?.action == MotionEvent.ACTION_UP && isViewInBounds(binding.trash, event.rawX.toInt(), event.rawY.toInt())){
                val parent = v?.parent as ViewManager
                parent.removeView(v)
                mIsScrolling = false
                binding.trash.visibility = View.GONE
                binding.layoutEdit.visibility = View.VISIBLE
                elementCount--
            }

            if(mGestureDetector.onTouchEvent(event)){
                return true
            }

            if(event?.action == MotionEvent.ACTION_UP){
                if(mIsScrolling){
                    mIsScrolling = false
                    binding.trash.visibility = View.GONE
                    binding.layoutEdit.visibility = View.VISIBLE
                }
            }
            return false
        }
    }

    private fun isViewInBounds(view: View, x: Int, y: Int): Boolean {
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    private fun checkSavedState(){
        if(elementCount != 0){
            AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Meme belum tersimpan, apakah anda yakin keluar ?")
                .setPositiveButton("Ya"
                ) { _, _ -> finish() }
                .setNegativeButton("Tidak", null)
                .show()
        }else{
            finish()
        }
    }

    override fun onBackPressed() {
        checkSavedState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            checkSavedState()
            return true
        }
        return false
    }

}