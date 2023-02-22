package com.example.catchtheball;

import android.view.MotionEvent;

public class InputListener {
    public enum Direction { NONE, UP, RIGHT, DOWN, LEFT }

    private static final int SWIPE_MIN_DISTANCE = 0;
    private static final int SWIPE_THRESHOLD_VELOCITY = 25;
    private static final int MOVE_THRESHOLD = 250;
    private static final int RESET_STARTING = 10;
    private float x;
    private float y;
    private float lastDx;
    private float lastDy;
    private float previousX;
    private float previousY;
    private float startingX;
    private float startingY;
    private int previousDirection = 1;
    private int veryLastDirection = 1;
    // Whether or not we have made a move, i.e. the blocks shifted or tried to shift.
    private boolean hasMoved = false;
    // Whether or not we began the press on an icon. This is to disable swipes if the user began
    // the press on an icon.
    private boolean beganOnIcon = false;

    public Direction handleEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                startingX = x;
                startingY = y;
                previousX = x;
                previousY = y;
                lastDx = 0;
                lastDy = 0;
                hasMoved = false;
                return Direction.NONE;
            case MotionEvent.ACTION_MOVE:
                Direction movement = Direction.NONE;
                x = event.getX();
                y = event.getY();
                if (!beganOnIcon) {
                    float dx = x - previousX;
                    if (Math.abs(lastDx + dx) < Math.abs(lastDx) + Math.abs(dx) && Math.abs(dx) > RESET_STARTING
                            && Math.abs(x - startingX) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastDx = dx;
                        previousDirection = veryLastDirection;
                    }
                    if (lastDx == 0) {
                        lastDx = dx;
                    }
                    float dy = y - previousY;
                    if (Math.abs(lastDy + dy) < Math.abs(lastDy) + Math.abs(dy) && Math.abs(dy) > RESET_STARTING
                            && Math.abs(y - startingY) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastDy = dy;
                        previousDirection = veryLastDirection;
                    }
                    if (lastDy == 0) {
                        lastDy = dy;
                    }

                    float pathMoved = (x - startingX) * (x - startingX) + (y - startingY) * (y - startingY);
                    if (pathMoved > SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE && !hasMoved) {
                        boolean moved = false;
                        // Vertical
                        boolean dyGeDx = Math.abs(dy) >= Math.abs(dx);
                        boolean dxGeDy = Math.abs(dx) >= Math.abs(dy);
                        if (((dy >= SWIPE_THRESHOLD_VELOCITY && dyGeDx) || y - startingY >= MOVE_THRESHOLD) && previousDirection % 2 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 2;
                            veryLastDirection = 2;
                            movement = Direction.DOWN;
                        } else if (((dy <= -SWIPE_THRESHOLD_VELOCITY && dyGeDx) || y - startingY <= -MOVE_THRESHOLD) && previousDirection % 3 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 3;
                            veryLastDirection = 3;
                            movement = Direction.UP;
                        }
                        // Horizontal
                        else if (((dx >= SWIPE_THRESHOLD_VELOCITY && dxGeDy) || x - startingX >= MOVE_THRESHOLD) && previousDirection % 5 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 5;
                            veryLastDirection = 5;
                            movement = Direction.RIGHT;
                        } else if (((dx <= -SWIPE_THRESHOLD_VELOCITY && dxGeDy) || x - startingX <= -MOVE_THRESHOLD) && previousDirection % 7 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 7;
                            veryLastDirection = 7;
                            movement = Direction.LEFT;
                        }
                        if (moved) {
                            hasMoved = true;
                            startingX = x;
                            startingY = y;
                        }
                    }
                }
                previousX = x;
                previousY = y;
                return movement;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                previousDirection = 1;
                veryLastDirection = 1;
        }
        return Direction.NONE;
    }
}