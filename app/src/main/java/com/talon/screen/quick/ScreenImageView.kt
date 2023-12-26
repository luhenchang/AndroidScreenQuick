package com.talon.screen.quick

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

interface CallBack {
    fun messageBack(event: MotionEvent)
}

class ScreenImageView : View {

    private var bitmap: Bitmap? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    lateinit var msgCallBack: CallBack
    fun setOnCallBack(msgCallBack: CallBack) {
       this.msgCallBack = msgCallBack
    }

    private fun init() {
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_background)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            val srcRect = Rect(0, 0, it.width, it.height)
            //1080 是原先大小
            //2352 是高度原先大小
            val destRect = Rect(0, 0, 1176, 2328)
            Log.e("绘制图片？", "true")
            canvas.drawBitmap(it, srcRect, destRect, Paint())
        }
    }

    fun setBitMap(bitmap: Bitmap) {
        Log.e("绘制图片？", "setBitMap")
        this.bitmap = bitmap
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 按下时的处理
                Log.e("坐标：：==", "" + event.x + "," + event.y)
                msgCallBack.messageBack(event)
            }

            MotionEvent.ACTION_UP -> {
                // 抬起时的处理
                Toast.makeText(context, "actiono_up", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}