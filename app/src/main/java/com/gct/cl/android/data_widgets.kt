package com.gct.cl.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.compose.ui.geometry.Rect

fun dp2pixel(dp: Float, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
}

class ChannelMessageWidget constructor(
    private val context: Context
) : View(context) {
    private var text: String = "null"
    private lateinit var avatar: Bitmap
    private var displayed = false
    private val radius = 64f
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 16f
    }

//    @SuppressLint("DrawAllocation")
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        val scaledBitmap = Bitmap.createScaledBitmap(avatar, dp2pixel(radius, context).toInt(), dp2pixel(radius, context).toInt(), true)
//
//        val outputBitmap = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
//
//        val paint = Paint().apply {
//            isAntiAlias = true
//            color = Color.BLACK
//            style = Paint.Style.FILL
//        }
//
//        val radius = scaledBitmap.width / 2f
//        canvas.drawCircle(radius, radius, radius, paint)
//
//        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//        canvas.drawBitmap(outputBitmap, 0f, 0f, paint)
//
//        val rect = Rect(0f, 0f, scaledBitmap.width.toFloat(), scaledBitmap.height.toFloat())
//
//        displayed = true
//    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scaledBitmap = Bitmap.createScaledBitmap(avatar, dp2pixel(radius, context).toInt(), dp2pixel(radius, context).toInt(), true)

        val outputBitmap = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
        val outputCanvas = Canvas(outputBitmap) // Создаем Canvas для outputBitmap

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        val radius = scaledBitmap.width / 2f
        outputCanvas.drawCircle(radius, radius, radius, paint) // Рисуем круг на outputCanvas

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        outputCanvas.drawBitmap(scaledBitmap, 0f, 0f, paint) // Рисуем отмасштабированное изображение на outputCanvas

        canvas.drawBitmap(outputBitmap, 0f, 0f, null) // Рисуем outputBitmap на холсте canvas

        canvas.drawText(text, 200f, 200f, textPaint)

        setMeasuredDimension(500, 1000)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(500, 500)
    }

//    @SuppressLint("DrawAllocation")
//    override fun onDraw(canvas: Canvas) {
//        val paint = Paint().apply {
//            isAntiAlias = true
//            color = Color.BLACK
//            style = Paint.Style.FILL
//        }
//
//        val rect = Rect(0, 0, 100, 50) // Прямоугольник с шириной экрана и высотой 50 пикселей
//
//        canvas.drawRect(rect, paint) // Отрисовка прямоугольника на холсте
//
//        super.onDraw(canvas)
//    }


    fun setAvatar(avatar: Bitmap) {
        this.avatar = avatar
    }

    fun setText(text: String) {
        this.text = text
    }
}