package com.example.catchtheball.sprite

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.game.rl.RLTask
import java.util.Random
import kotlin.math.min;


class Ball(
    private val ballColor: Int = Color.WHITE,
    private val ballRadius: Float = 10.0f,
    // The ball speed is sampled at every throw from the range [minSpeed, maxSpeed].
    private val minSpeed: Int = 10000,
    private val maxSpeed: Int = 10000,
    private val rand: Random = Random()
) : Sprite() {

    private var screenWidth: Int = -1
    private var screenHeight: Int = -1

    private val _paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ballColor
    }

    private val speed: Float
        get() {
            val s =  (minSpeed..maxSpeed).random().toFloat()
            return s
        }

    // Valid range [0, screenWidth].
    private var x: Int = -1
    // Valid range [0, screenHeight].
    private var y: Int = 0

    fun getX(): Int { return x }
    fun getY(): Int { return y }
    fun getRadius(): Float { return ballRadius }

    fun reset() {
        x = rand.nextInt(screenWidth)
        y = 0
    }

    override fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    override fun update(timeDelta: Float) {
        // Initialize the position if not yet set.
        if (x < 0) x = screenWidth / 2

        y = min(y + (speed * timeDelta).toInt(), screenHeight)
    }

    override fun draw(c: Canvas) {
        RLTask.get().logExtra("ball", "[${x}, ${y}]")
        c.drawCircle( x.toFloat(), y.toFloat(), ballRadius, _paint)
    }
}