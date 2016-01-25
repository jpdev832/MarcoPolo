package com.staticvillage.marcopolo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by joelparrish on 11/2/15.
 */
public class PoloView extends SurfaceView implements SurfaceHolder.Callback{
    private RunnerThread mThread;
    private SurfaceHolder mHolder;
    private Drawer mDrawer;

    public PoloView(Context context) {
        super(context);
        init(null);
    }

    public PoloView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PoloView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Initialize view
     */
    private void init(AttributeSet attrs) {
        Log.d("lemi", "initializing view");
        setZOrderOnTop(true);

        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        mHolder.addCallback(this);
    }

    private void startThread() {
        Log.d("lemi", "startThread: ");
        mThread = new RunnerThread(mDrawer, getHolder());
        mThread.start();
    }

    private void stopThread() {
        if(mThread == null)
            return;

        try {
            mThread.cancel();
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setDrawer(Drawer drawer) {
        if(mThread != null) {
			mThread.cancel();
			try {
				mThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				mThread = null;
			}
		}

        mDrawer = drawer;
        startThread();
    }

    private class RunnerThread extends Thread {
        private final Drawer mmDrawer;
        private final SurfaceHolder mmHolder;
        private boolean mmRunning;

        public RunnerThread(Drawer drawer, SurfaceHolder holder) {
            mmDrawer = drawer;
            mmHolder = holder;
            mmRunning = true;
        }

        @Override
        public void run() {
            Canvas canvas = null;

            while (mmRunning) {
                try {
                    canvas = mmHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (mmHolder) {
                            mmDrawer.draw(canvas);
                        }
                    }
                } finally {
                    if (canvas != null)
                        mmHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        public void cancel() {
            mmRunning = false;
        }
    }
}
