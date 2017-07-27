package com.wordpress.simpledevelopments.liligame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by connor on 7/26/17.
 */

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "GameSurfaceView";

    private SurfaceHolder surfaceHolder;
    private DrawThread drawThread;

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

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
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
            Canvas canvas;
            Paint paint = new Paint();
            paint.setColor(Color.CYAN);

            while (running) {
                canvas = null;
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    //do drawing
                    canvas.drawRect(0,0,canvas.getWidth(), canvas.getHeight(), paint);
                }
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

        }
    }
}
