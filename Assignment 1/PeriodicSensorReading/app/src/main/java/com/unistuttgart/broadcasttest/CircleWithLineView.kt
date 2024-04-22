package com.unistuttgart.broadcasttest
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.content.ContextCompat

/**
 * Custom view that displays a circle with a line that rotates based on the angle.
 * @param context The context in which the view is created.
 * @param attrs The attributes of the view.
 */
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

    // Set the color of the line based on the current mode (light/dark).
    init {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> setColor(ContextCompat.getColor(context, R.color.white))
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> setColor(ContextCompat.getColor(context, R.color.black))
        }
    }

    private fun setColor(color: Int) {
        paint.color = color
        invalidate()
    }

    /**
     * Set the angle of the line in degrees.
     * @param angle The angle in degrees.
     */
    fun setAngle(angle: Float) {
        this.angle = (angle - 90) % 360
        invalidate()
    }

    /**
     * Calculate the new position of the line based on the angle.
     * @param w The new width of the view.
     * @param h The new height of the view.
     * @param oldw The old width of the view.
     * @param oldh The old height of the view.
     * @see View.onSizeChanged
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.set(w / 2f, h / 2f)
        radius = Math.min(w, h) / 2f - 5
        width = w.toFloat()
        height = h.toFloat()
    }

    /**
     * Draw the circle, line, and cardinal directions on the canvas.
     * @param canvas The canvas on which to draw.
     * @see View.onDraw
     */
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