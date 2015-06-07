/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialprogressbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;

abstract class ProgressDrawableBase extends Drawable {

    protected boolean mUseIntrinsicPadding = true;
    protected int mAlpha = 0xFF;
    protected ColorFilter mColorFilter;
    protected ColorStateList mTint;
    protected PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;
    protected PorterDuffColorFilter mTintFilter;
    protected int mLayoutDirection;
    protected boolean mAutoMirrored = true;

    private Paint mPaint;

    public ProgressDrawableBase(Context context) {
        int colorControlActivated = getThemeAttrColor(context, R.attr.colorControlActivated);
        // setTint() has been overridden for compatibility; DrawableCompat won't work because
        // wrapped Drawable won't be Animatable.
        setTint(colorControlActivated);
    }

    private static int getThemeAttrColor(Context context, int attr) {
        TypedArray a = context.obtainStyledAttributes(null, new int[]{attr});
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    public boolean getUseIntrinsicPadding() {
        return mUseIntrinsicPadding;
    }

    public void setUseIntrinsicPadding(boolean useIntrinsicPadding) {
        if (mUseIntrinsicPadding != useIntrinsicPadding) {
            mUseIntrinsicPadding = useIntrinsicPadding;
            invalidateSelf();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlpha(int alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            invalidateSelf();
        }
    }

    /**
     * {@inheritDoc}
     */
    // Rewrite for compatibility.
    @Override
    public void setTint(int tint) {
        setTintList(ColorStateList.valueOf(tint));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mColorFilter = colorFilter;
        invalidateSelf();
    }

    @Override
    public void setTintList(ColorStateList tint) {
        mTint = tint;
        mTintFilter = makeTintFilter(tint, mTintMode);
        invalidateSelf();
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        mTintMode = tintMode;
        mTintFilter = makeTintFilter(mTint, tintMode);
        invalidateSelf();
    }

    private PorterDuffColorFilter makeTintFilter(ColorStateList tint, PorterDuff.Mode tintMode) {

        if (tint == null || tintMode == null) {
            return null;
        }

        int color = tint.getColorForState(getState(), Color.TRANSPARENT);
        // They made PorterDuffColorFilter.setColor() and setMode() @hide.
        return new PorterDuffColorFilter(color, tintMode);
    }

    /**
     * Returns the resolved layout direction for this Drawable.
     *
     * @return One of {@link android.view.View#LAYOUT_DIRECTION_LTR},
     *   {@link android.view.View#LAYOUT_DIRECTION_RTL}
     */
    public int getLayoutDirection() {
        return mLayoutDirection;
    }

    /**
     * Set the layout direction for this drawable. Should be a resolved direction as the
     * Drawable as no capacity to do the resolution on his own.
     *
     * @param layoutDirection One of {@link android.view.View#LAYOUT_DIRECTION_LTR},
     *   {@link android.view.View#LAYOUT_DIRECTION_RTL}
     */
    public void setLayoutDirection(int layoutDirection) {
        if (getLayoutDirection() != layoutDirection) {
            mLayoutDirection = layoutDirection;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoMirrored() {
        return mAutoMirrored;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutoMirrored(boolean autoMirrored) {
        if (mAutoMirrored != autoMirrored) {
            mAutoMirrored = autoMirrored;
            invalidateSelf();
        }
    }

    private boolean needMirroring() {
        return isAutoMirrored() && getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(Canvas canvas) {

        Rect bounds = getBounds();
        if (bounds.width() == 0 || bounds.height() == 0) {
            return;
        }

        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.BLACK);
            onPreparePaint(mPaint);
        }
        mPaint.setAlpha(mAlpha);
        ColorFilter colorFilter = mColorFilter != null ? mColorFilter : mTintFilter;
        mPaint.setColorFilter(colorFilter);

        int saveCount = canvas.save();

        canvas.translate(bounds.left, bounds.top);
        if (needMirroring()) {
            canvas.translate(bounds.width(), 0);
            canvas.scale(-1, 1);
        }

        onDraw(canvas, bounds.width(), bounds.height(), mPaint);

        canvas.restoreToCount(saveCount);
    }

    abstract protected void onPreparePaint(Paint paint);

    abstract protected void onDraw(Canvas canvas, int width, int height, Paint paint);
}
