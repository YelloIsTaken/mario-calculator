package com.mario.calculator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View

class MarioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private val COLOR_BG              = Color.parseColor("#080E1C")
        private val COLOR_STAR            = Color.parseColor("#FFFFCC")
        private val COLOR_PIPE_DARK       = Color.parseColor("#007700")
        private val COLOR_PIPE_LIGHT      = Color.parseColor("#00AA00")
        private val COLOR_PIPE_EDGE       = Color.parseColor("#00CC00")
        private val COLOR_PLATFORM_BASE   = Color.parseColor("#4878C0")
        private val COLOR_PLATFORM_BRICK  = Color.parseColor("#2858A0")
        private val COLOR_PLATFORM_TOP    = Color.parseColor("#90B8F0")
        private val COLOR_MARIO_RED       = Color.parseColor("#CC2200")
        private val COLOR_MARIO_SKIN      = Color.parseColor("#FFBB88")
        private val COLOR_MARIO_EYES      = Color.parseColor("#111111")
        private val COLOR_MARIO_MUSTACHE  = Color.parseColor("#884400")
        private val COLOR_MARIO_OVERALLS  = Color.parseColor("#0033BB")
        private val COLOR_MARIO_SHOES     = Color.parseColor("#4A2800")
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var marioX = 0f
    private var walkFrame = 0
    private val handler = Handler(Looper.getMainLooper())

    private val starPositions = listOf(
        0.08f to 0.12f, 0.22f to 0.22f, 0.40f to 0.08f,
        0.55f to 0.30f, 0.68f to 0.14f, 0.80f to 0.35f,
        0.15f to 0.45f, 0.88f to 0.10f, 0.32f to 0.40f
    )

    private val animRunnable = object : Runnable {
        override fun run() {
            marioX += dpToPx(2f)
            walkFrame = (walkFrame + 1) % 4
            if (marioX > width + dpToPx(24f)) marioX = -dpToPx(24f)
            invalidate()
            handler.postDelayed(this, 120)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(animRunnable)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(animRunnable)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        marioX = dpToPx(30f)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val platformH = h * 0.38f
        val platformY = h - platformH

        drawBackground(canvas, w, h)
        drawStars(canvas, w, platformY)
        drawPipe(canvas, w, h, platformH, platformY)
        drawPlatform(canvas, w, h, platformY, platformH)
        drawMario(canvas, marioX, platformY, platformH)
    }

    private fun drawBackground(canvas: Canvas, w: Float, h: Float) {
        paint.color = COLOR_BG
        canvas.drawRect(0f, 0f, w, h, paint)
    }

    private fun drawStars(canvas: Canvas, w: Float, platformY: Float) {
        paint.color = COLOR_STAR
        val px = dpToPx(2f)
        starPositions.forEach { (xr, yr) ->
            val sx = xr * w
            val sy = yr * platformY
            canvas.drawRect(sx, sy, sx + px, sy + px, paint)
        }
    }

    private fun drawPipe(canvas: Canvas, w: Float, h: Float, platformH: Float, platformY: Float) {
        val pipeW = w * 0.11f
        val pipeX = w - pipeW - dpToPx(8f)
        val capH = platformH * 0.28f
        val bodyTop = platformY * 0.30f
        val capTop = bodyTop - capH

        paint.color = COLOR_PIPE_DARK
        canvas.drawRect(pipeX, bodyTop, pipeX + pipeW, platformY, paint)
        paint.color = COLOR_PIPE_LIGHT
        canvas.drawRect(pipeX + 2f, bodyTop, pipeX + pipeW * 0.35f, platformY, paint)

        val capX = pipeX - pipeW * 0.15f
        paint.color = COLOR_PIPE_DARK
        canvas.drawRect(capX, capTop, capX + pipeW * 1.3f, bodyTop, paint)
        paint.color = COLOR_PIPE_LIGHT
        canvas.drawRect(capX + 2f, capTop, capX + pipeW * 0.45f, bodyTop, paint)
        paint.color = COLOR_PIPE_EDGE
        canvas.drawRect(capX, capTop, capX + pipeW * 1.3f, capTop + dpToPx(2f), paint)
    }

    private fun drawPlatform(canvas: Canvas, w: Float, h: Float, platformY: Float, platformH: Float) {
        paint.color = COLOR_PLATFORM_BASE
        canvas.drawRect(0f, platformY, w, h, paint)

        val brickW = w / 9f
        val brickH = platformH / 2f
        paint.color = COLOR_PLATFORM_BRICK

        var bx = 0f
        while (bx < w) {
            canvas.drawRect(bx, platformY, bx + brickW - dpToPx(1f), platformY + brickH - dpToPx(1f), paint)
            bx += brickW
        }
        bx = -brickW / 2f
        while (bx < w) {
            canvas.drawRect(bx, platformY + brickH, bx + brickW - dpToPx(1f), h - dpToPx(1f), paint)
            bx += brickW
        }

        paint.color = COLOR_PLATFORM_TOP
        canvas.drawRect(0f, platformY, w, platformY + dpToPx(2f), paint)
    }

    private fun drawMario(canvas: Canvas, x: Float, platformY: Float, platformH: Float) {
        val spriteH = platformH * 0.88f
        val s = spriteH / 16f
        val y = platformY - spriteH
        val isWalking = walkFrame == 1 || walkFrame == 3

        paint.color = COLOR_MARIO_RED
        canvas.drawRect(x + 3*s, y, x + 13*s, y + 3*s, paint)
        canvas.drawRect(x + 2*s, y + 3*s, x + 14*s, y + 4.5f*s, paint)

        paint.color = COLOR_MARIO_SKIN
        canvas.drawRect(x + 2*s, y + 4.5f*s, x + 14*s, y + 8*s, paint)

        paint.color = COLOR_MARIO_EYES
        canvas.drawRect(x + 5*s, y + 5*s, x + 7*s, y + 7*s, paint)
        canvas.drawRect(x + 10*s, y + 5*s, x + 12*s, y + 7*s, paint)

        paint.color = COLOR_MARIO_MUSTACHE
        canvas.drawRect(x + 3*s, y + 7*s, x + 13*s, y + 8.5f*s, paint)

        paint.color = COLOR_MARIO_RED
        canvas.drawRect(x + 1*s, y + 8.5f*s, x + 15*s, y + 12*s, paint)

        paint.color = COLOR_MARIO_OVERALLS
        canvas.drawRect(x + 4*s, y + 10*s, x + 7*s, y + 16*s, paint)
        canvas.drawRect(x + 9*s, y + 10*s, x + 12*s, y + 16*s, paint)
        canvas.drawRect(x + 1*s, y + 12*s, x + 15*s, y + 14*s, paint)

        paint.color = COLOR_MARIO_SHOES
        if (isWalking) {
            canvas.drawRect(x + 1*s, y + 13.5f*s, x + 7*s, y + 16*s, paint)
            canvas.drawRect(x + 9*s, y + 14.5f*s, x + 15*s, y + 16*s, paint)
        } else {
            canvas.drawRect(x + 1*s, y + 14*s, x + 7*s, y + 16*s, paint)
            canvas.drawRect(x + 9*s, y + 14*s, x + 15*s, y + 16*s, paint)
        }
    }

    private fun dpToPx(dp: Float): Float =
        dp * resources.displayMetrics.density
}
