package com.unistuttgart.broadcasttest
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class CircleWithLineView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        textSize = 100f
    }
    private val center = PointF()
    private var radius = 0f
    private var angle = 0f
    private var width = 0f
    private var height = 0f

    fun setAngle(angle: Float) {
        this.angle = (angle - 90) % 360
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.set(w / 2f, h / 2f)
        radius = Math.min(w, h) / 2f - 5
        width = w.toFloat()
        height = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(center.x, center.y, radius, paint)

        canvas.drawText("N", width/2f - 35f, 100f, paint)
        canvas.drawText("S", width/2f - 35f, height - 20f, paint)
        canvas.drawText("W", 20f, height/2f + 35f, paint)
        canvas.drawText("E", width - 90f, height/2f + 35f, paint)

        val endX = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val endY = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()
        canvas.drawLine(center.x, center.y, endX, endY, paint)
    }
}