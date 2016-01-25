package com.staticvillage.marcopolo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.DisplayMetrics;

/**
 * Created by joelparrish.
 */
public class PoloDrawer implements Drawer{
    private UberPuff mUberPuff;

    public PoloDrawer(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mUberPuff = new UberPuff(activity, metrics.density);
    }

    /**
     * Set a new moment to be drawn
     * @param bitmap moment
     */
    public synchronized void setMoment(Bitmap bitmap) {
        if(bitmap != null)
            mUberPuff.addImage(bitmap);
        else
            mUberPuff.removeImage();
    }

    /**
     * Set new message to be displayed
     * @param message message
     */
    public synchronized void setMessage(String message) {
        mUberPuff.setMessage(message);
    }

    /**
     * draw graphics to the canvas
     * @param canvas canvas
     */
    @Override
    public void draw(Canvas canvas) {canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.TRANSPARENT);

        Bitmap scene = mUberPuff.getScene();
        Rect srcRect = new Rect(0, canvas.getHeight() - scene.getHeight(), canvas.getWidth(),
                canvas.getHeight());
        canvas.drawBitmap(mUberPuff.getScene(), mUberPuff.getSceneRect(), srcRect, null);
    }
}
