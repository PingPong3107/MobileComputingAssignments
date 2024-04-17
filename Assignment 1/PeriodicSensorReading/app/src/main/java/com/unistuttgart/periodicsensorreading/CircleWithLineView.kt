package com.unistuttgart.periodicsensorreading
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
        strokeWidth = 4f
    }
    private val center = PointF()
    private var radius = 0f
    private var angle = 0f

    fun setAngle(angle: Float) {
        this.angle = (angle - 90) % 360
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.set(w / 2f, h / 2f)
        radius = Math.min(w, h) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(center.x, center.y, radius, paint)
        val endX = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val endY = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()
        canvas.drawLine(center.x, center.y, endX, endY, paint)
    }
}