package com.bsk.floatingbubblelib;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;

import androidx.core.content.ContextCompat;

/**
 * Floating configurations
 * Created by bijoysingh on 2/19/17.
 */

public class FloatingBubbleConfig {
  private Drawable bubbleIcon;
  private Drawable removeBubbleIcon;
  private View expandableView;
  private int bubbleIconDp;
  private int removeBubbleIconDp;
  private float removeBubbleAlpha;
  private int expandableColor;
  private int triangleColor;
  private int gravity;
  private int paddingDp;
  private int borderRadiusDp;
  private int bubbleGravity;
  private int bubbleYOffset;
  private int bubbleXOffset;
  private boolean physicsEnabled;

  private FloatingBubbleConfig(Builder builder) {
    bubbleIcon = builder.bubbleIcon;
    removeBubbleIcon = builder.removeBubbleIcon;
    expandableView = builder.expandableView;
    bubbleIconDp = builder.bubbleIconDp;
    removeBubbleIconDp = builder.removeBubbleIconDp;
    expandableColor = builder.expandableColor;
    triangleColor = builder.triangleColor;
    gravity = builder.gravity;
    paddingDp = builder.paddingDp;
    borderRadiusDp = builder.borderRadiusDp;
    physicsEnabled = builder.physicsEnabled;
    removeBubbleAlpha = builder.removeBubbleAlpha;
    bubbleGravity = builder.bubbleGravity;
    bubbleYOffset = builder.bubbleYOffset;
    bubbleXOffset = builder.bubbleXOffset;
  }

  public static Builder getDefaultBuilder(Context context) {
    return new Builder()
        .bubbleIcon(ContextCompat.getDrawable(context, R.drawable.bubble_default_icon))
        .removeBubbleIcon(ContextCompat.getDrawable(context, R.drawable.close_default_icon))
        .bubbleIconDp(64)
        .removeBubbleIconDp(64)
        .paddingDp(4)
        .removeBubbleAlpha(1.0f)
        .physicsEnabled(true)
        .expandableColor(Color.WHITE)
        .triangleColor(Color.WHITE)
        .gravity(Gravity.END);
  }

  public static FloatingBubbleConfig getDefault(Context context) {
    return getDefaultBuilder(context).build();
  }

  public Drawable getBubbleIcon() {
    return bubbleIcon;
  }

  public Drawable getRemoveBubbleIcon() {
    return removeBubbleIcon;
  }

  public View getExpandableView() {
    return expandableView;
  }

  public int getBubbleIconDp() {
    return bubbleIconDp;
  }

  public int getRemoveBubbleIconDp() {
    return removeBubbleIconDp;
  }

  public int getExpandableColor() {
    return expandableColor;
  }

  public int getTriangleColor() {
    return triangleColor;
  }

  public int getGravity() {
    return gravity;
  }

  public int getBubbleGravity() {
    return bubbleGravity;
  }

  public int getBubbleYOffset() {
    return bubbleYOffset;
  }

  public int getBubbleXOffset() {
    return bubbleXOffset;
  }

  public int getPaddingDp() {
    return paddingDp;
  }

  public boolean isPhysicsEnabled() {
    return physicsEnabled;
  }

  public int getBorderRadiusDp() {
    return borderRadiusDp;
  }

  public float getRemoveBubbleAlpha() {
    return removeBubbleAlpha;
  }

  public static final class Builder {
    private Drawable bubbleIcon;
    private Drawable removeBubbleIcon;
    private View expandableView;
    private int bubbleIconDp = 64;
    private int removeBubbleIconDp = 64;
    private int expandableColor = Color.WHITE;
    private int triangleColor = Color.WHITE;
    private int gravity = Gravity.END;
    private int paddingDp = 4;
    private int borderRadiusDp = 4;
    private float removeBubbleAlpha = 1.0f;
    private boolean physicsEnabled = true;
    private int bubbleGravity = Gravity.TOP | Gravity.START;
    private int bubbleYOffset = 0;
    private int bubbleXOffset = 0;

    public Builder() {
    }

    public Builder bubbleIcon(Drawable val) {
      bubbleIcon = val;
      return this;
    }

    public Builder removeBubbleIcon(Drawable val) {
      removeBubbleIcon = val;
      return this;
    }

    public Builder expandableView(View val) {
      expandableView = val;
      return this;
    }

    public Builder bubbleIconDp(int val) {
      bubbleIconDp = val;
      return this;
    }

    public Builder removeBubbleIconDp(int val) {
      removeBubbleIconDp = val;
      return this;
    }

    public Builder triangleColor(int val) {
      triangleColor = val;
      return this;
    }

    public Builder expandableColor(int val) {
      expandableColor = val;
      return this;
    }

    public FloatingBubbleConfig build() {
      return new FloatingBubbleConfig(this);
    }

    public Builder gravity(int val) {
      gravity = val;
      if (gravity == Gravity.CENTER ||
          gravity == Gravity.CENTER_VERTICAL ||
          gravity == Gravity.CENTER_HORIZONTAL) {
        gravity = Gravity.CENTER_HORIZONTAL;
      } else if (gravity == Gravity.TOP ||
          gravity == Gravity.BOTTOM) {
        gravity = Gravity.END;
      }
      return this;
    }

    public Builder paddingDp(int val) {
      paddingDp = val;
      return this;
    }

    public Builder borderRadiusDp(int val) {
      borderRadiusDp = val;
      return this;
    }

    public Builder physicsEnabled(boolean val) {
      physicsEnabled = val;
      return this;
    }

    public Builder removeBubbleAlpha(float val) {
      removeBubbleAlpha = val;
      return this;
    }

    public Builder bubbleGravity(int gravity) {
      bubbleGravity = gravity;
      return this;
    }

    /**
     * This is in regards with the bubble gravity & acts as the offset corresponding to the
     * gravity that is set.
     */
    public Builder bubbleYOffset(int value) {
      bubbleYOffset = value;
      return this;
    }

    public Builder bubbleXOffset(int value) {
      bubbleXOffset = value;
      return this;
    }
  }


}
