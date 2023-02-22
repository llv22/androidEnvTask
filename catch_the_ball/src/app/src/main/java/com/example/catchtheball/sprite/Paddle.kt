package com.example.catchtheball.sprite

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import com.example.catchtheball.InputListener
import com.google.game.rl.RLTask
import kotlin.math.max
import kotlin.math.min

class Paddle(
    private val paddleColor: Int = Color.WHITE,
    // Width of the paddle in pixels.
    private val width: Int = 80,
    // Height of the paddle in pixels.
    private val height: Int = 10
) : Sprite() {

    private var screenWidth: Int = -1
    private var screenHeight: Int = -1

    private val rect = Rect()

    private val inputListener: InputListener = InputListener()
    private val swipeDisplacement: Int
        get() {
            return (width * 0.75).toInt();
        }
    private var isDragging: Boolean = false

    private val _paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = paddleColor
    }

    // Valid range [0, screenWidth].
    private var x: Int = -1
    // Valid range [0, screenHeight].
    private var y: Int = -1

    fun getX(): Int { return x }
    fun getWidth(): Int { return width }

    fun touch(event: MotionEvent) {
        when (RLTask.get().get("interfaceType", "raw")) {
            "raw" -> {
                this.x = event.x.toInt()
            }
            "fling" -> {
                when (inputListener.handleEvent(event)) {
                    InputListener.Direction.LEFT -> this.x = max(0, this.x - swipeDisplacement)
                    InputListener.Direction.RIGHT -> this.x = min(screenWidth, this.x + swipeDisplacement)
                    else -> return
                }
            }
            "tap" -> {
                if (event.action == MotionEvent.ACTION_DOWN) this.x = event.x.toInt()
            }
            "drag" -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val halfWidth = width / 2
                        if ((event.x >= x - halfWidth) && (event.x <= x + halfWidth)) {
                            isDragging = true
                        }
                    }
                    MotionEvent.ACTION_UP -> isDragging = false
                    MotionEvent.ACTION_MOVE -> {
                        if (isDragging) this.x = event.x.toInt()
                    }
                    else -> isDragging = false
                }
            }
        }
    }

    override fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    override fun update(timeDelta: Float) {
        // Initialize the position if not yet set.
        if (x < 0) x = screenWidth / 2
        if (y < 0) y = screenHeight

        rect.bottom = y
        rect.top = rect.bottom - height
        rect.left = max(0, x - (width / 2))
        rect.right = min(screenWidth, rect.left + width)
    }

    override fun draw(c: Canvas) {
        RLTask.get().logExtra("paddle", "[${rect.centerX()}, ${rect.centerY()}]")
        RLTask.get().logExtra("paddle_width", "[${width}]")
        c.drawRect(rect, _paint)
    }
}