package com.github.omadahealth.lollipin.lib.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.github.omadahealth.lollipin.lib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stoyan and oliviergoutay
 * @version 1/13/15
 */
public class PinCodeRoundView extends RelativeLayout {

    private Context mContext;
    private List<ImageView> mRoundViews;
    private int mCurrentLength;
    private Drawable mEmptyDotDrawableId;
    private Drawable mFullDotDrawableId;
    private ViewGroup mRoundContainer;

    public PinCodeRoundView(Context context) {
        this(context, null);
    }

    public PinCodeRoundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinCodeRoundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context;
        initializeView(attrs, defStyleAttr);
    }

    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null && !isInEditMode()) {
            final TypedArray attributes = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.PinCodeView,
                    defStyleAttr, 0);

            int color = attributes.getColor(R.styleable.PinCodeView_lp_dominant_color, ContextCompat.getColor(mContext, R.color.light_blue_500));
 
            GradientDrawable emptyDotDrawable = new GradientDrawable();
            emptyDotDrawable.setColor(ColorUtils.blendARGB(color, Color.WHITE, 0.4f));
            emptyDotDrawable.setShape(GradientDrawable.OVAL);
            mEmptyDotDrawableId = emptyDotDrawable;

            GradientDrawable fullDotDrawable = new GradientDrawable();
            fullDotDrawable.setColor(ColorUtils.blendARGB(color, Color.BLACK, 0.4f));
            fullDotDrawable.setShape(GradientDrawable.OVAL);
            mFullDotDrawableId = fullDotDrawable;

            attributes.recycle();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            PinCodeRoundView view = (PinCodeRoundView) inflater.inflate(R.layout.view_round_pin_code, this);
            mRoundContainer = (ViewGroup) view.findViewById( R.id.round_container );

            mRoundViews = new ArrayList<>();
        }
    }

    /**
     * Refresh the {@link android.widget.ImageView}s to look like what typed the user
     *
     * @param pinLength the current pin code length typed by the user
     */
    public void refresh(int pinLength) {
        mCurrentLength = pinLength;
        for (int i = 0; i < mRoundViews.size(); i++) {
            if (pinLength - 1 >= i) {
                mRoundViews.get(i).setImageDrawable(mFullDotDrawableId);
            } else {
                mRoundViews.get(i).setImageDrawable(mEmptyDotDrawableId);
            }
        }
    }

    public int getCurrentLength() {
        return mCurrentLength;
    }

    /**
     * Sets a custom empty dot drawable for the {@link ImageView}s.
     * @param drawable the resource Id for a custom drawable
     */
    public void setEmptyDotDrawable(Drawable drawable) {
        mEmptyDotDrawableId = drawable;
    }

    /**
     * Sets a custom full dot drawable for the {@link ImageView}s.
     * @param drawable the resource Id for a custom drawable
     */
    public void setFullDotDrawable(Drawable drawable) {
        mFullDotDrawableId = drawable;
    }

    /**
     * Sets a custom empty dot drawable for the {@link ImageView}s.
     * @param drawableId the resource Id for a custom drawable
     */
    public void setEmptyDotDrawable(int drawableId) {
        mEmptyDotDrawableId = getResources().getDrawable(drawableId);
    }

    /**
     * Sets a custom full dot drawable for the {@link ImageView}s.
     * @param drawableId the resource Id for a custom drawable
     */
    public void setFullDotDrawable(int drawableId) {
        mFullDotDrawableId = getResources().getDrawable(drawableId);
    }

    /**
     * Sets the length of the pin code.
     *
     * @param pinLength the length of the pin code
     */
    public void setPinLength(int pinLength) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoundContainer.removeAllViews();
        List<ImageView> temp = new ArrayList<>(pinLength);
        for (int i = 0; i < pinLength; i++) {
            ImageView roundView;
            if (i < mRoundViews.size()) {
                roundView = mRoundViews.get(i);
            } else {
                roundView = (ImageView) inflater.inflate(R.layout.view_round, mRoundContainer, false);
            }
            mRoundContainer.addView(roundView);
            temp.add(roundView);
        }
        mRoundViews.clear();
        mRoundViews.addAll(temp);
        refresh(0);
    }
}
