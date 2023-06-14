package com.bsk.floatingbubblelib;

import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * FLoating Bubble Physics
 * Created by bijoysingh on 2/19/17.
 */

public class FloatingBubblePhysics extends DefaultFloatingBubbleTouchListener {

  private int sizeX;
  private int sizeY;
  private View bubbleView;
  private WindowManager windowManager;
  private FloatingBubbleConfig config;

  private WindowManager.LayoutParams bubbleParams;
  private FloatingBubbleAnimator animator;
  private Point[] previous = {null, null};

  private FloatingBubblePhysics(Builder builder) {
    sizeX = builder.sizeX;
    sizeY = builder.sizeY;
    bubbleView = builder.bubbleView;
    windowManager = builder.windowManager;
    config = builder.config;

    bubbleParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
    animator = new FloatingBubbleAnimator.Builder()
        .bubbleParams(bubbleParams)
        .bubbleView(bubbleView)
        .sizeX(sizeX)
        .sizeY(sizeY)
        .windowManager(windowManager)
        .build();
  }

  @Override
  public void onDown(float x, float y) {
    super.onDown(x, y);
    previous[0] = null;
    previous[1] = new Point((int) x, (int) y);
  }

  @Override
  public void onMove(float x, float y) {
    super.onMove(x, y);
    addSelectively(x, y);
  }

  @Override
  public void onUp(float x, float y) {
    addSelectively(x, y);
    Log.d(FloatingBubblePhysics.class.getSimpleName(), previous.toString());

    if (previous[0] == null) {
      moveToCorner();
    } else {
      moveLinearlyToCorner();
    }
  }

  private void moveLinearlyToCorner() {
    int x1 = previous[0].x;
    int y1 = previous[0].y;
    int x2 = previous[1].x;
    int y2 = previous[1].y;

    if (x2 == x1) {
      moveToCorner();
      return;
    }

    int xf = x1 < x2 ? sizeX - bubbleView.getWidth() : 0;
    int yf = y1 + (y2 - y1) * (xf - x1) / (x2 - x1);
    animator.animate(xf, yf);
  }

  private void moveToCorner() {
    if (previous[1].x < sizeX / 2) {
      animator.animate(0, previous[1].y);
    } else {
      animator.animate(sizeX - bubbleView.getWidth(), previous[1].y);
    }
  }

  private void addSelectively(float x, float y) {
    if (previous[1] != null && previous[1].x == (int) x && previous[1].y == (int) y) {
      return;
    }

    previous[0] = previous[1];
    previous[1] = new Point((int) x, (int) y);
  }

  public static final class Builder {
    private int sizeX;
    private int sizeY;
    private View bubbleView;
    private WindowManager windowManager;
    private FloatingBubbleConfig config;

    public Builder() {
    }

    public Builder sizeX(int val) {
      sizeX = val;
      return this;
    }

    public Builder sizeY(int val) {
      sizeY = val;
      return this;
    }

    public Builder bubbleView(View val) {
      bubbleView = val;
      return this;
    }

    public Builder windowManager(WindowManager val) {
      windowManager = val;
      return this;
    }

    public Builder config(FloatingBubbleConfig val) {
      config = val;
      return this;
    }

    public FloatingBubblePhysics build() {
      return new FloatingBubblePhysics(this);
    }
  }
}
