package com.wordpress.simpledevelopments.liligame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by connor on 7/26/17.
 */

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "GameSurfaceView";

    private SurfaceHolder surfaceHolder;
    private DrawThread drawThread;
    private GestureDetector gestureDetector;
    private TouchStates touchState;
    private float dragStartX;
    private float dragStartY;
    private float startRx;
    private float startRy;
    private int rx = 0;
    private int ry = 0;
    private double px = 0;
    private double py = 0;
    private boolean moved = true;

    private enum TouchStates {
        DRAGGING,
        IDLE
    }

    public GameSurfaceView(Context context) {
        super(context);
        init();
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Set the position of Lili
     * @param rx new x-coordinate
     * @param ry new y-coordinate
     */
    private void setRxy(int rx, int ry) {
        moved = true;
        if (rx + 800 > getWidth()) {
            this.rx = getWidth() - 800;
        } else if (rx < 0){
            this.rx = 0;
        } else {
            this.rx = rx;
        }
        if (ry + 600 > getHeight()) {
            this.ry = getHeight() - 600;
        } else if (ry < 0) {
            this.ry = 0;
        } else {
            this.ry = ry;
        }
    }
    private void setPxy(int px, int py) {
        this.px = px;
        this.py = py;
    }
    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        gestureDetector = new GestureDetector(getContext(),new GestureListener());
        touchState = TouchStates.IDLE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getX() >= rx
                    && event.getX() <= (rx + 800)
                    && event.getY() >= ry
                    && event.getY() <= (ry + 600)) {
                touchState = TouchStates.DRAGGING;
                dragStartX = event.getX();
                dragStartY = event.getY();
                startRx = rx;
                startRy = ry;
                result = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (touchState == TouchStates.DRAGGING) {
                float deltaX = (event.getX() - dragStartX);
                float deltaY = (event.getY() - dragStartY);

                setRxy((int) (startRx + deltaX), (int) (startRy + deltaY));
//                setPxy(0,0);
                result = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP){
            touchState = TouchStates.IDLE;
        }
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        if (drawThread == null) {
            drawThread = new DrawThread(holder);
            drawThread.setRunning(true);
            drawThread.start();
        } else {
            Log.e(TAG, "DrawThread is not == null!");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        boolean trying = true;
        drawThread.setRunning(false);
        while (trying) {
            try {
                drawThread.join();
                trying = false;
            } catch (InterruptedException ex) {
                Log.e(TAG, "Interrupted Exception while cancelling drawThread");
            }
        }
        drawThread = null;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "Fling of velocity: (" + velocityX + "," + velocityY + ")");
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private class DrawThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean running;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            this.running = false;
        }

        public void setRunning(boolean running){
            this.running = running;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            Paint paint = new Paint();
            paint.setColor(Color.CYAN);
            Drawable liliDrawable = ContextCompat.getDrawable(getContext(), R.drawable.lili);
            long time = System.currentTimeMillis();
            double m = 1.0;

            while (running) {
                double delta = (System.currentTimeMillis() - time) / 50d;
                time = System.currentTimeMillis();

                // If Lili is not being dragged around
                if (touchState != TouchStates.DRAGGING) {
                    // Momentum Calculations
                    if (rx + 800 < getWidth()) {
                        px = (px + 0 * delta);
                        rx = (int) (rx + px * delta / m);
                        if (rx + 800 > getWidth()) {
                            rx = getWidth() - 800;
                        }
                        if (px != 0) {
                            moved = true;
                        }
                    }
                    if (ry + 600 < getHeight()) {
                        py = (py + 9.8 * delta);
                        ry = (int) (ry + py * delta / m);
                        if (ry + 600 > getHeight()) {
                            ry = getHeight() - 600;
                        }
                        if (py != 0) {
                            moved = true;
                        }
                    }
                }




                if (moved) {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas == null) {
//                        continue;
                        Log.d(TAG, "Canvas is null!");
                        System.exit(-1);
                    }

                    synchronized (surfaceHolder) {
                        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
                        liliDrawable.setBounds(rx, ry, (800 + rx), (600 + ry));
                        liliDrawable.draw(canvas);
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas);
//                    Log.d(TAG, "Changed!");
                    moved = false;
                }

            }

        }
    }
}
