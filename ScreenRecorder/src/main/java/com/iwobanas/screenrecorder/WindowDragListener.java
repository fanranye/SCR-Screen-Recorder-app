package com.iwobanas.screenrecorder;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

class WindowDragListener implements View.OnTouchListener {
    private static final String TAG = "scr_WindowDragListener";
    private int dragStartX;
    private int dragStartY;
    private boolean dragging;
    private WindowManager.LayoutParams params;

    private OnWindowDragStartListener startListener;
    private OnWindowDragEndListener endListener;

    WindowDragListener(WindowManager.LayoutParams params) {
        this.params = params;
    }

    /*
     * motionEvent.getY() - coordinates relative to view
     * motionEvent.getRawY() - raw screen coordinates
     * dragStartY - relative coordinates of grab location
     */

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (!dragging) {
                    dragStartX = (int) motionEvent.getX();
                    dragStartY = (int) motionEvent.getY();
                    if (startListener != null) {
                        startListener.onDragStart();
                    }
                    dragging = true;
                    return true;
                }

                Rect frame = new Rect();
                view.getWindowVisibleDisplayFrame(frame);

                int leftX, topY, centerX, centerY, rightX, bottomY, gravity = 0;

                leftX = (int) motionEvent.getRawX() - dragStartX - frame.left;
                centerX = (int) motionEvent.getRawX() - dragStartX - (frame.centerX() - view.getWidth() / 2);
                rightX = frame.right - (int) motionEvent.getRawX() - view.getWidth() + dragStartX;

                topY = (int) motionEvent.getRawY() - dragStartY - frame.top;
                centerY = (int) motionEvent.getRawY() - dragStartY - (frame.centerY() - view.getHeight() / 2);
                bottomY = frame.bottom - (int) motionEvent.getRawY() - view.getHeight() + dragStartY;

                if (leftX <= Math.abs(centerX) && leftX <= rightX) {
                    params.x = Math.max(leftX, 0);
                    gravity |= Gravity.LEFT;
                } else if (rightX <= Math.abs(centerX) && rightX <= leftX) {
                    params.x = Math.max(rightX, 0);
                    gravity |= Gravity.RIGHT;
                } else {
                    params.x = centerX;
                    gravity |= Gravity.CENTER_HORIZONTAL;
                }

                if (topY <= Math.abs(centerY) && topY <= bottomY) {
                    params.y = Math.max(topY, 0);
                    gravity |= Gravity.TOP;
                } else if (bottomY <= Math.abs(centerY) && bottomY <= topY) {
                    params.y = Math.max(bottomY, 0);
                    gravity |= Gravity.BOTTOM;
                } else {
                    params.y = centerY;
                    gravity |= Gravity.CENTER_VERTICAL;
                }

                params.gravity = gravity;

                try {
                    getWindowManager(view.getContext()).updateViewLayout(view, params);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error updating layout", e);
                    return false;
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging) {
                    dragging = false;
                    if (endListener != null) {
                        endListener.onDragEnd();
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    private WindowManager getWindowManager(Context context) {
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void setDragStartListener(OnWindowDragStartListener startListener) {
        this.startListener = startListener;
    }

    public void setDragEndListener(OnWindowDragEndListener endListener) {
        this.endListener = endListener;
    }

    public interface OnWindowDragStartListener {
        void onDragStart();
    }

    public interface OnWindowDragEndListener {
        void onDragEnd();
    }
}