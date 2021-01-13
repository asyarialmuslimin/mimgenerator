package com.saifur.mimgenerator.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.dinuscxj.gesture.MultiTouchGestureDetector
import com.dinuscxj.gesture.MultiTouchGestureDetector.SimpleOnMultiTouchGestureListener


@SuppressLint("ViewConstructor")
class MultiTouchGestureView(context: Context, attrs: AttributeSet?, drawable: Drawable) :
    View(context, attrs) {
    private val mMultiTouchGestureDetector: MultiTouchGestureDetector
    private val mIcon: Drawable
    private var mScaleFactor = 1.0f
    private var mOffsetX = 0.0f
    private var mOffsetY = 0.0f
    private var mRotation = 0.0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mMultiTouchGestureDetector.onTouchEvent(event)

        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        // move to center
        canvas.translate(
            ((measuredWidth - mIcon.intrinsicWidth) / 2).toFloat(),
            ((measuredHeight - mIcon.intrinsicHeight) / 2).toFloat()
        )

        // transform
        canvas.save()
        canvas.translate(mOffsetX, mOffsetY)
        canvas.scale(
            mScaleFactor,
            mScaleFactor,
            (mIcon.intrinsicWidth / 2).toFloat(),
            (mIcon.intrinsicHeight / 2).toFloat()
        )
        canvas.rotate(mRotation, (mIcon.intrinsicWidth / 2).toFloat(),
            (mIcon.intrinsicHeight / 2).toFloat()
        )
        mIcon.draw(canvas)
        canvas.restore()
        canvas.restore()
    }

    private inner class MultiTouchGestureDetectorListener :
        SimpleOnMultiTouchGestureListener() {
        override fun onScale(detector: MultiTouchGestureDetector) {
            mScaleFactor *= detector.scale
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 5.0f))
            invalidate()
        }

        override fun onMove(detector: MultiTouchGestureDetector) {
            mOffsetX += detector.moveX
            mOffsetY += detector.moveY
            invalidate()
        }

        override fun onRotate(detector: MultiTouchGestureDetector) {
            mRotation += detector.rotation
            invalidate()
        }
    }

    init {
        mMultiTouchGestureDetector =
            MultiTouchGestureDetector(context, MultiTouchGestureDetectorListener())
        mIcon = drawable
        mIcon.setBounds(0, 0, mIcon.intrinsicWidth, mIcon.intrinsicHeight)
    }
}