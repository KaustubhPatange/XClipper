package com.bsk.floatingbubblelib;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Touch event for the Floating Bubble Service
 * Created by bijoysingh on 2/19/17.
 */

public class FloatingBubbleTouch implements View.OnTouchListener {

  private static final int TOUCH_CLICK_TIME = 250;
  private static final float EXPANSION_FACTOR = 1.25f;

  private int sizeX;
  private int sizeY;

  private View bubbleView;
  private View removeBubbleView;
  private View expandableView;
  private WindowManager windowManager;
  private FloatingBubbleTouchListener listener;
  private FloatingBubbleTouchListener physics;
  private int removeBubbleSize;
  private FloatingBubbleConfig config;
  private int padding;
  private int marginBottom;

  private WindowManager.LayoutParams bubbleParams;
  private WindowManager.LayoutParams removeBubbleParams;
  private WindowManager.LayoutParams expandableParams;
  private int removeBubbleStartSize;
  private int removeBubbleExpandedSize;
  private FloatingBubbleAnimator animator;

  private long touchStartTime = 0;
  private long lastTouchTime = 0;
  private boolean expanded = false;

  private FloatingBubbleTouch(Builder builder) {
    padding = builder.padding;
    config = builder.config;
    removeBubbleSize = builder.removeBubbleSize;
    physics = builder.physics;
    listener = builder.listener;
    windowManager = builder.windowManager;
    expandableView = builder.expandableView;
    removeBubbleView = builder.removeBubbleView;
    bubbleView = builder.bubbleView;
    sizeY = builder.sizeY;
    sizeX = builder.sizeX;
    marginBottom = builder.marginBottom;

    bubbleParams = (WindowManager.LayoutParams) bubbleView.getLayoutParams();
    removeBubbleParams = (WindowManager.LayoutParams) removeBubbleView.getLayoutParams();
    expandableParams = (WindowManager.LayoutParams) expandableView.getLayoutParams();
    removeBubbleStartSize = removeBubbleSize;
    removeBubbleExpandedSize = (int) (EXPANSION_FACTOR * removeBubbleSize);
    animator = new FloatingBubbleAnimator.Builder()
        .sizeX(sizeX)
        .sizeY(sizeY)
        .windowManager(windowManager)
        .bubbleView(bubbleView)
        .bubbleParams(bubbleParams)
        .build();

    View rootLayout = expandableView.findViewById(R.id.root_linearLayout);
    rootLayout.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        setState(false);
        return false;
      }
    });
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    switch (motionEvent.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        touchStartTime = System.currentTimeMillis();
        if (listener != null) {
          listener.onDown(motionEvent.getRawX(), motionEvent.getRawY());
        }
        if (sendEventToPhysics()) {
          physics.onDown(motionEvent.getRawX(), motionEvent.getRawY());
        }
        break;

      case MotionEvent.ACTION_MOVE:
        lastTouchTime = System.currentTimeMillis();
        moveBubbleView(motionEvent);
        if (lastTouchTime - touchStartTime > TOUCH_CLICK_TIME) {
          compressView();
          showRemoveBubble(View.VISIBLE);
        }

        if (listener != null) {
          listener.onMove(motionEvent.getRawX(), motionEvent.getRawY());
        }
        if (sendEventToPhysics()) {
          physics.onMove(motionEvent.getRawX(), motionEvent.getRawY());
        }
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        showRemoveBubble(View.GONE);
        lastTouchTime = System.currentTimeMillis();
        if (lastTouchTime - touchStartTime < TOUCH_CLICK_TIME) {
          toggleView();
          if (listener != null) {
            listener.onTap(expanded);
          }
          if (sendEventToPhysics()) {
            physics.onTap(expanded);
          }
        } else {
          boolean isRemoved = checkRemoveBubble();
          if (listener != null) {
            listener.onUp(motionEvent.getRawX(), motionEvent.getRawY());
          }
          if (!isRemoved && sendEventToPhysics()) {
            physics.onUp(motionEvent.getRawX(), motionEvent.getRawY());
          }
        }
    }
    return true;
  }

  private void moveBubbleView(MotionEvent motionEvent) {
    float halfClipSize = bubbleView.getWidth() / 2;
    float clipSize = bubbleView.getWidth();

    float leftX = motionEvent.getRawX() - halfClipSize;
    leftX = (leftX > sizeX - clipSize) ? (sizeX - clipSize) : leftX;
    leftX = leftX < 0 ? 0 : leftX;

    float topY = motionEvent.getRawY() - halfClipSize;
    topY = (topY > sizeY - clipSize) ? (sizeY - clipSize) : topY;
    topY = topY < 0 ? 0 : topY;

    bubbleParams.x = (int) leftX;
    bubbleParams.y = (int) topY;

    handleRemove();
    windowManager.updateViewLayout(bubbleView, bubbleParams);
    windowManager.updateViewLayout(removeBubbleView, removeBubbleParams);
  }

  private void handleRemove() {
    if (isInsideRemoveBubble()) {
      removeBubbleParams.height = removeBubbleExpandedSize;
      removeBubbleParams.width = removeBubbleExpandedSize;
      removeBubbleParams.x = (sizeX - removeBubbleParams.width) / 2;
      removeBubbleParams.y = sizeY - removeBubbleParams.height - marginBottom;
      bubbleParams.x = removeBubbleParams.x + (removeBubbleExpandedSize - bubbleView.getWidth()) / 2;
      bubbleParams.y = removeBubbleParams.y + (removeBubbleExpandedSize - bubbleView.getWidth()) / 2;
    } else {
      removeBubbleParams.height = removeBubbleStartSize;
      removeBubbleParams.width = removeBubbleStartSize;
      removeBubbleParams.x = (sizeX - removeBubbleParams.width) / 2;
      removeBubbleParams.y = sizeY - removeBubbleParams.height - marginBottom;
    }
  }

  private boolean isInsideRemoveBubble() {
    int bubbleSize = removeBubbleView.getWidth() == 0
        ? removeBubbleStartSize
        : removeBubbleView.getWidth();
    int top = removeBubbleParams.y;
    int right = removeBubbleParams.x + bubbleSize;
    int bottom = removeBubbleParams.y + bubbleSize;
    int left = removeBubbleParams.x;

    int centerX = bubbleParams.x + bubbleView.getWidth() / 2;
    int centerY = bubbleParams.y + bubbleView.getWidth() / 2;

    return centerX > left && centerX < right && centerY > top && centerY < bottom;
  }

  private boolean checkRemoveBubble() {
    if (isInsideRemoveBubble()) {
      if (listener != null) {
        listener.onRemove();
      }
      if (sendEventToPhysics()) {
        physics.onRemove();
      }
      return true;
    }
    return false;
  }

  private boolean sendEventToPhysics() {
    return config.isPhysicsEnabled() && physics != null;
  }

  private void showRemoveBubble(int visibility) {
    removeBubbleView.setVisibility(visibility);
  }

  public void setState(boolean state) {
    expanded = state;
    if (expanded) {
      expandView();
    } else {
      compressView();
    }
  }

  private void toggleView() {
    expanded = !expanded;
    setState(expanded);
  }

  private void compressView() {
    expandableView.setVisibility(View.GONE);
  }

  private void expandView() {
    int x = 0;
    int y = padding;
    switch (config.getGravity()) {
      case Gravity.CENTER:
      case Gravity.CENTER_HORIZONTAL:
      case Gravity.CENTER_VERTICAL:
        x = (sizeX - bubbleView.getWidth()) / 2;
        break;
      case Gravity.LEFT:
      case Gravity.START:
        x = padding;
        break;
      case Gravity.RIGHT:
      case Gravity.END:
        x = sizeX - bubbleView.getWidth() - padding;
        break;
    }

    animator.animate(x, y);
    expandableView.setVisibility(View.VISIBLE);
    expandableParams.y = y + bubbleView.getWidth();
    windowManager.updateViewLayout(expandableView, expandableParams);
  }

  public static final class Builder {
    private int sizeX;
    private int sizeY;
    private View bubbleView;
    private View removeBubbleView;
    private View expandableView;
    private FloatingBubbleLogger logger;
    private WindowManager windowManager;
    private FloatingBubbleTouchListener listener;
    private int removeBubbleSize;
    private FloatingBubbleTouchListener physics;
    private FloatingBubbleConfig config;
    private int padding;
    private int marginBottom;

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

    public Builder removeBubbleView(View val) {
      removeBubbleView = val;
      return this;
    }

    public Builder expandableView(View val) {
      expandableView = val;
      return this;
    }

    public Builder logger(FloatingBubbleLogger val) {
      logger = val;
      return this;
    }

    public Builder windowManager(WindowManager val) {
      windowManager = val;
      return this;
    }

    public FloatingBubbleTouch build() {
      return new FloatingBubbleTouch(this);
    }

    public Builder removeBubbleSize(int val) {
      removeBubbleSize = val;
      return this;
    }

    public Builder physics(FloatingBubbleTouchListener val) {
      physics = val;
      return this;
    }

    public Builder listener(FloatingBubbleTouchListener val) {
      listener = val;
      return this;
    }

    public Builder config(FloatingBubbleConfig val) {
      config = val;
      return this;
    }

    public Builder padding(int val) {
      padding = val;
      return this;
    }

    public Builder marginBottom(int val) {
      marginBottom = val;
      return this;
    }
  }
}
