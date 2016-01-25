package com.staticvillage.marcopolo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;

/**
 * Created by joelparrish.
 */
public class UberPuff {
    public static final int DP_SCENE_WIDTH = 360;
    public static final int DP_SCENE_HEIGHT = 300;

    public static final int DP_CLOUDY_WIDTH = 185;
    public static final int DP_CLOUDY_HEIGHT = 170;
    public static final int DP_CLOUDY_X = 0;
    public static final int DP_CLOUDY_Y = 102;

    public static final int DP_IMAGE_FRAME_WIDTH = 150;
    public static final int DP_IMAGE_FRAME_HEIGHT = 170;
    public static final int DP_IMAGE_FRAME_X = 175;
    public static final int DP_IMAGE_FRAME_Y = 0;
    public static final int DP_IMAGE_WIDTH = 135;
    public static final int DP_IMAGE_HEIGHT = 135;
    public static final int DP_IMAGE_X = DP_IMAGE_FRAME_X + 7;
    public static final int DP_IMAGE_Y = DP_IMAGE_FRAME_Y + 7;

    public static final int DP_MESSAGE_X = 108;
    public static final int DP_MESSAGE_Y = 132;
    public static final int DP_MESSAGE_TEXT_X = 164;
    public static final int DP_MESSAGE_TEXT_Y = 204;
    public static final int DP_MESSAGE_TEXT_WIDTH = 176;
    public static final int DP_MESSAGE_TEXT_HEIGHT = 66;

    private final Bitmap mBitmapMessage;
    private final Bitmap mBitmapUberPuff;
    private final Context mContext;
    private final Paint mWhitePaint;
    private final TextPaint mTextPaint;

    private Bitmap mBitmapImage;
    private Bitmap mBitmapTmpImage;
    private Bitmap mBitmapScene;
    private DynamicLayout mMessageLayout;
    private Rect mSceneRect;
    private Rect mUberPuffDestRect;
    private Rect mUberPuffSrcRect;
    private String mMessageText;
    private float mDensity;
    private int mImageOffset;
    private int mSceneWidth;
    private int mSceneHeight;
    private int mUberPuffWidth;
    private long mEndTime;

    /**
     * Contruct a new Uber Puff
     * @param context context
     * @param density screen density
     */
    public UberPuff(Context context, float density) {
        this.mContext = context;
        this.mDensity = density;

        mWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setStyle(Paint.Style.FILL);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(getDensityPixel(14));

        mSceneWidth = getDensityPixel(DP_SCENE_WIDTH);
        mSceneHeight = getDensityPixel(DP_SCENE_HEIGHT);
        mUberPuffWidth = getDensityPixel(DP_CLOUDY_WIDTH);

        mBitmapUberPuff = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cloudy);
        mBitmapMessage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.message);

        mBitmapScene = Bitmap.createBitmap(mSceneWidth, mSceneHeight, Bitmap.Config.ARGB_8888);

        mUberPuffDestRect = new Rect(getDensityPixel(DP_CLOUDY_X),
                getDensityPixel(DP_CLOUDY_Y),
                getDensityPixel(DP_CLOUDY_X) + mUberPuffWidth,
                getDensityPixel(DP_CLOUDY_Y) + mBitmapUberPuff.getHeight());

        mSceneRect = new Rect(0, 0, mSceneWidth, mSceneHeight);
        mMessageText = "";
    }

    /**
     * Convert density independent pixel into a density pixel
     * @param pixel density independent pixel
     * @return density pixel
     */
    public int getDensityPixel(int pixel) {
        return (int) (pixel * mDensity);
    }

    /**
     * Retrieve current animation frame
     * @return animation frame
     */
    public int currentFrame() {
        long now = System.currentTimeMillis();
        if(now > mEndTime) {
            if(mBitmapTmpImage != null) {
                mBitmapImage = mBitmapTmpImage;
                mBitmapTmpImage = null;
            }

            return 3;
        }

        double diff = (mEndTime - now) / 1000.0;

        if (diff < 0.25)
            return 0;
        else if (diff < 0.5)
            return 1;
        else if (diff < 0.75)
            return 2;
        else
            return 3;
    }

    /**
     * Update Uber Puff scene
     */
    public void update() {
        int offset = mUberPuffWidth * currentFrame();
        mUberPuffSrcRect = new Rect(offset, 0, offset + mUberPuffWidth, mBitmapUberPuff.getHeight());

        mImageOffset = getDensityPixel(150) - (currentFrame() * getDensityPixel(50));
    }

    /**
     * Retrieve Scene Rect bounds
     * @return Scene Rect bounds
     */
    public Rect getSceneRect() {
        return mSceneRect;
    }

    /**
     * Add new image for Uber Puff
     * @param bitmap image
     */
    public void addImage(Bitmap bitmap) {
        if(bitmap != null)
            mBitmapTmpImage = Bitmap.createScaledBitmap(bitmap, getDensityPixel(DP_IMAGE_WIDTH),
                    getDensityPixel(DP_IMAGE_HEIGHT), false);
        else
            mBitmapImage = null;

        mEndTime = System.currentTimeMillis() + 1000;
    }

    /**
     * Remove Uber Puff's image
     */
    public void removeImage() {
        mBitmapImage = null;
    }

    /**
     * Uber Puff message
     * @param message
     */
    public void setMessage(String message) {
        mMessageText = message;
    }

    /**
     * Create Uber Puff scene
     * @return Uber Puff scene
     */
    public Bitmap getScene() {
        update();

        Canvas canvas = new Canvas(mBitmapScene);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(mBitmapUberPuff, mUberPuffSrcRect, mUberPuffDestRect, null);

        if(mBitmapImage != null && currentFrame() != 0) {
            canvas.drawRect(getDensityPixel(DP_IMAGE_FRAME_X),
                    getDensityPixel(DP_IMAGE_FRAME_Y) + mImageOffset,
                    getDensityPixel(DP_IMAGE_FRAME_X + DP_IMAGE_FRAME_WIDTH),
                    getDensityPixel(DP_IMAGE_FRAME_Y + DP_IMAGE_FRAME_HEIGHT) + mImageOffset,
                    mWhitePaint);
            canvas.drawBitmap(mBitmapImage, getDensityPixel(DP_IMAGE_X),
                    getDensityPixel(DP_IMAGE_Y) + mImageOffset, null);
        }

        canvas.drawBitmap(mBitmapMessage, getDensityPixel(DP_MESSAGE_X),
                getDensityPixel(DP_MESSAGE_Y) , null);
        mMessageLayout = new DynamicLayout(mMessageText, mTextPaint,
                getDensityPixel(DP_MESSAGE_TEXT_WIDTH), Layout.Alignment.ALIGN_CENTER, 1, 1, true);

        canvas.save();
        float textXCoordinate = getDensityPixel(DP_MESSAGE_TEXT_X);
        canvas.translate(textXCoordinate, getDensityPixel(DP_MESSAGE_TEXT_Y));
        mMessageLayout.draw(canvas);
        canvas.restore();

        return mBitmapScene;
    }
}
