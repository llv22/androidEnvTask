package com.example.catchtheball.sprite

import android.graphics.Canvas

abstract class Sprite {

    open fun setScreenSize(width: Int, height: Int) {}
    open fun update(timeDelta: Float) {}
    open fun draw(c: Canvas) {}
}