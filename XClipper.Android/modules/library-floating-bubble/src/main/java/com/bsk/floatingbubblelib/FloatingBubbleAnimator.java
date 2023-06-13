package com.bsk.floatingbubblelib;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;

/**
 * Animator
 * Created by bijoysingh on 2/19/17.
 */

public class FloatingBubbleAnimator {

  private static final int ANIMATION_TIME = 100;
  private static final int ANIMATION_STEPS = 5;

  private View bubbleView;
  private WindowManager.LayoutParams bubbleParams;
  private WindowManager windowManager;
  private int sizeX;
  private int sizeY;
  private float x, y;
  private ValueAnimator animator;

  private FloatingBubbleAnimator(Builder builder) {
    bubbleView = builder.bubbleView;
    bubbleParams = builder.bubbleParams;
    windowManager = builder.windowManager;
    sizeX = builder.sizeX;
    sizeY = builder.sizeY;
    animator = ValueAnimator.ofInt(0, 10).setDuration(ANIMATION_TIME);
    animator.addUpdateListener(AnimationChangeListener);
  }

  public void animate(final float x, final float y) {
    this.x = x;
    this.y = y;
    if (animator.isRunning())
      animator.cancel();
    animator.start();
//    ValueAnimator animator = ValueAnimator.ofInt(0, 10)
//        .setDuration(ANIMATION_TIME);
//    animator.addUpdateListener();
//    animator.start();
  }

  public final ValueAnimator.AnimatorUpdateListener AnimationChangeListener = new ValueAnimator.AnimatorUpdateListener() {
    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
      final float startX = bubbleParams.x;
      final float startY = bubbleParams.y;

      try {
        float currentX = startX + ((x - startX) *
                (Integer) valueAnimator.getAnimatedValue() / ANIMATION_STEPS);
        float currentY = startY + ((y - startY) *
                (Integer) valueAnimator.getAnimatedValue() / ANIMATION_STEPS);
        bubbleParams.x = (int) currentX;
        bubbleParams.x = bubbleParams.x < 0 ? 0 : bubbleParams.x;
        bubbleParams.x = bubbleParams.x > sizeX - bubbleView.getWidth() ? sizeX - bubbleView.getWidth() : bubbleParams.x;

        bubbleParams.y = (int) currentY;
        bubbleParams.y = bubbleParams.y < 0 ? 0 : bubbleParams.y;
        bubbleParams.y = bubbleParams.y > sizeY - bubbleView.getWidth() ? sizeY - bubbleView.getWidth() : bubbleParams.y;

        windowManager.updateViewLayout(bubbleView, bubbleParams);
      } catch (Exception exception) {
        Log.e(FloatingBubbleAnimator.class.getSimpleName(), exception.getMessage());
      }
    }
  };

  public static final class Builder {
    private View bubbleView;
    private WindowManager.LayoutParams bubbleParams;
    private WindowManager windowManager;
    private int sizeX;
    private int sizeY;

    public Builder() {
    }

    public Builder bubbleView(View val) {
      bubbleView = val;
      return this;
    }

    public Builder bubbleParams(WindowManager.LayoutParams val) {
      bubbleParams = val;
      return this;
    }

    public Builder windowManager(WindowManager val) {
      windowManager = val;
      return this;
    }

    public Builder sizeX(int val) {
      sizeX = val;
      return this;
    }

    public Builder sizeY(int val) {
      sizeY = val;
      return this;
    }

    public FloatingBubbleAnimator build() {
      return new FloatingBubbleAnimator(this);
    }
  }
}
