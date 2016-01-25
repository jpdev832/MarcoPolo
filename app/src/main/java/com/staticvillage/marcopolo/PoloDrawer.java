package com.staticvillage.marcopolo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.DisplayMetrics;

/**
 * Created by joelparrish on 11/17/15.
 */
public class PoloDrawer implements Drawer{
    public static final int IMAGE_FRAME_WIDTH = 150;
    public static final int IMAGE_FRAME_HEIGHT = 170;
    public static final int IMAGE_FRAME_X = 175;
    public static final int IMAGE_FRAME_Y = 323;
    public static final int IMAGE_WIDTH = 135;
    public static final int IMAGE_HEIGHT = 135;
    public static final int IMAGE_X = IMAGE_FRAME_X + 7;
    public static final int IMAGE_Y = IMAGE_FRAME_Y + 7;
    public static final int CLOUDY_WIDTH = 185;
    public static final int CLOUDY_X = 0;
    public static final int CLOUDY_Y = 420;
    public static final int MESSAGE_X = 108;
    public static final int MESSAGE_Y = 453;

    private Bitmap mBitmapImage;
    private Bitmap mBitmapCloudy;
    private Bitmap mBitmapMessage;
    private Paint mWhitePaint;
    private int mCloudyWidth;
    private float mDensity;
    private long mStartTime;
    private long mEndTime;
    private Rect mCloudyRect;
    private Rect mCloudySrcRect;
    private Rect mMessageSrcRect;

    public PoloDrawer(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mDensity = metrics.density;
        mBitmapCloudy = BitmapFactory.decodeResource(activity.getResources(), R.drawable.cloudy);
        mBitmapMessage = BitmapFactory.decodeResource(activity.getResources(), R.drawable.message);
        mWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setStyle(Paint.Style.FILL);
        mCloudyWidth = getDensityPixel(CLOUDY_WIDTH);
        mStartTime = System.currentTimeMillis();
        mEndTime = mStartTime;

        mCloudySrcRect = new Rect(getDensityPixel(CLOUDY_X),
                getDensityPixel(CLOUDY_Y),
                getDensityPixel(CLOUDY_X) + mCloudyWidth,
                getDensityPixel(CLOUDY_Y) + mBitmapCloudy.getHeight());
        mMessageSrcRect = new Rect(getDensityPixel(MESSAGE_X),
                getDensityPixel(MESSAGE_Y),
                getDensityPixel(MESSAGE_X) + mBitmapMessage.getWidth(),
                getDensityPixel(MESSAGE_Y) + mBitmapMessage.getHeight());
    }

    public synchronized void setBitmap(Bitmap bitmap) {
        mBitmapImage = Bitmap.createScaledBitmap(bitmap, getDensityPixel(IMAGE_WIDTH),
                getDensityPixel(IMAGE_HEIGHT), false);

        mStartTime = System.currentTimeMillis();
        mEndTime = mStartTime + 2000;
    }

    private int getDensityPixel(int pixel) {
        return (int)(pixel * mDensity);
    }

    public int getCloudyState() {
        long now = System.currentTimeMillis();
        if(now > mEndTime)
            return 3;

        double diff = (mEndTime - now) / 2000.0;

        if (diff < 0.25)
            return 0;
        else if (diff < 0.5)
            return 1;
        else if (diff < 0.75)
            return 2;
        else
            return 3;
    }

    private void update(long gametime) {
        int offset = mCloudyWidth * getCloudyState();
        mCloudyRect = new Rect(offset, 0, offset + mCloudyWidth, mBitmapCloudy.getHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        update(System.currentTimeMillis());

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(mBitmapCloudy, mCloudyRect, mCloudySrcRect, null);
        canvas.drawBitmap(mBitmapMessage, getDensityPixel(MESSAGE_X), getDensityPixel(MESSAGE_Y), null);

        if(mBitmapImage != null) {
            canvas.drawRect((int) (IMAGE_FRAME_X * mDensity),
                    getDensityPixel(IMAGE_FRAME_Y),
                    getDensityPixel(IMAGE_FRAME_X + IMAGE_FRAME_WIDTH),
                    getDensityPixel(IMAGE_FRAME_Y + IMAGE_FRAME_HEIGHT), mWhitePaint);
            canvas.drawBitmap(mBitmapImage, getDensityPixel(IMAGE_X), getDensityPixel(IMAGE_Y), null);
        }
    }
}
