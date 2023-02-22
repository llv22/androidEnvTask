package com.example.catchtheball.sprite

import android.graphics.Canvas
import android.graphics.Color
import com.example.catchtheball.sprite.Sprite


class Background(private val color: Int = Color.BLACK) : Sprite() {

    override fun draw(c: Canvas) {
        c.drawColor(color)
    }
}