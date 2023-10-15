package com.gct.cl.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.TypedValue
import android.view.View


fun dp2pixel(dp: Float, context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics
    )
}

@SuppressLint("ViewConstructor")
class ChannelMessageWidget(
    private val context: Context, private val screenWidth: Int
) : View(context) {
    private var text: String = "null"
    private lateinit var avatar: Bitmap
    private var displayed = false
    private val radius = 48F
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 48F
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scaledBitmap = Bitmap.createScaledBitmap(
            avatar, dp2pixel(radius, context).toInt(), dp2pixel(radius, context).toInt(), true
        )

        val outputBitmap =
            Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
        val outputCanvas = Canvas(outputBitmap) // Создаем Canvas для outputBitmap

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.FILL
        }


        val radius = scaledBitmap.width / 2F
        outputCanvas.drawCircle(radius, radius, radius, paint) // Рисуем круг на outputCanvas

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        outputCanvas.drawBitmap(
            scaledBitmap, 0F, 0F, paint
        ) // Рисуем отмасштабированное изображение на outputCanvas

        canvas.drawBitmap(outputBitmap, 0F, 0F, null) // Рисуем outputBitmap на холсте canvas

        canvas.drawText(text, 200F, 100F, textPaint)

        setMeasuredDimension((screenWidth * 0.75F).toInt(), 500)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension((screenWidth * 0.75F).toInt(), 500)
    }


    fun setAvatar(avatar: Bitmap) {
        this.avatar = avatar
    }

    fun setText(text: String) {
        this.text = text
    }
}