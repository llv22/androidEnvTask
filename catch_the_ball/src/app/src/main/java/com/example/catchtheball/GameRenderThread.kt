package com.example.catchtheball

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.SystemClock
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.catchtheball.sprite.Background
import com.example.catchtheball.sprite.Ball
import com.example.catchtheball.sprite.Paddle
import com.example.catchtheball.sprite.Sprite
import com.google.game.rl.RLTask
import java.util.Random

class GameRenderThread(
    var holder: SurfaceHolder,
    private val hardwareAccelerated: Boolean = false
) : Thread(GameRenderThread::class.java.simpleName) {

    companion object {
        const val LIVES = 0
    }

    private var _lives: MutableLiveData<Int> = MutableLiveData<Int>()
        .apply { value = RLTask.get().get("lives", LIVES) }
    val lives: LiveData<Int> = _lives

    private var _quit = false

    private var _score: MutableLiveData<Int> = MutableLiveData<Int>()
        .apply { value = 0 }
    val score: LiveData<Int> = _score

    private val _gameOver: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
        .apply {
            value = false
        }

    val gameOver: LiveData<Boolean> = _gameOver

    private var _width: Int = 0
    private var _height: Int = 0

    private val rand = Random()

    private val _sprites: MutableList<Sprite> = mutableListOf(
        Background(color = Color.parseColor(RLTask.get().get("backgroundColor", "black"))),
        newBall(),
        Paddle(
            paddleColor = Color.parseColor(RLTask.get().get("paddleColor", "white")),
            width = RLTask.get().get("paddleWidth", 80),
            height = RLTask.get().get("paddleHeight", 10)
        )
    )

    private val paddle: Paddle
        get() { return _sprites[2] as Paddle }
    private val ball: Ball
        get() { return _sprites[1] as Ball }

    private fun newBall(): Sprite {
        val ballColor = Color.parseColor(RLTask.get().get("ballColor", "white"));
        val ballRadius = RLTask.get().get("ballRadius", 10.0f);
        val minSpeed = RLTask.get().get("minSpeed", 10_000);
        val maxSpeed = RLTask.get().get("maxSpeed", 10_000);
        return Ball(
            ballColor = ballColor,
            ballRadius = ballRadius,
            minSpeed = minSpeed,
            maxSpeed = maxSpeed,
            rand = rand
        )
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        paddle.touch(event)
        return true
    }

    fun setSize(width: Int, height: Int) {
        _width = width
        _height = height
        _sprites.forEach { sprite -> sprite.setScreenSize(width, height) }
        ball.reset()
    }

    override fun run() {
        super.run()
        _quit = false
        var currentTime = SystemClock.elapsedRealtime().toFloat()
        // Lower sleepTimeMs lead to smoother animations.
        val sleepTimeMs = 33L;
        while (!_quit) {
            val newTime = SystemClock.elapsedRealtime().toFloat()
            val elapsed = (newTime - currentTime) / 100_000.0f
            currentTime = newTime
            integrate(elapsed)
            render()
            sleep(sleepTimeMs)
        }
    }

    private fun integrate(timeDelta: Float) {
        _sprites.forEach { sprite -> sprite.update(timeDelta) }

        val result = if (ball.getY() + ball.getRadius() >= _height) {
            // Simple collision detection that just checks the x coordinate.
            val halfPaddle = paddle.getWidth() / 2
            if (ball.getX() >= (paddle.getX() - halfPaddle) &&
                ball.getX() <= (paddle.getX() + halfPaddle)) {
                1
            } else {
                -1
            }
        } else {
            0
        }

        var scoreValue: Int = _score.value ?: 0
        var livesValue: Int = _lives.value ?: RLTask.get().get("lives", LIVES)
        if (result < 0) { // game lost
            scoreValue += result
            _score.postValue(scoreValue)
            RLTask.get().logScore(scoreValue)
            if (livesValue <= 0) {
                _gameOver.postValue(true)
                RLTask.get().logEpisodeEnd()
                _quit = true
            } else {
                livesValue--
                _lives.postValue(livesValue)
                RLTask.get().logExtra("lives", "[$livesValue]")
                ball.reset()
            }
        } else if (result > 0) { // won
            scoreValue += result
            _score.postValue(scoreValue)
            RLTask.get().logScore(scoreValue)
            ball.reset()
        }
    }

    private fun render() {
        val c: Canvas = if (hardwareAccelerated) {
            holder.lockHardwareCanvas()
        } else {
            holder.lockCanvas()
        }
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        _sprites.forEach { sprite -> sprite.draw(c) }
        holder.unlockCanvasAndPost(c)
    }

    fun onResume() {
        _quit = false
    }

    fun onPause() {
        _quit = true
    }

    fun quit() {
        _quit = true
        try {
            this.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
