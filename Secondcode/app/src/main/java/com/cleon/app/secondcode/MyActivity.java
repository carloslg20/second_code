package com.cleon.app.secondcode;

import android.animation.Animator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;

import java.util.Random;

public class MyActivity extends Activity implements View.OnClickListener,
        View.OnLongClickListener, View.OnDragListener {

    private static final String TAG = "MyActivity";

    private static final String VIEW_TAG = "view_tag";

    private static final int OFF_SET = 200;

    private Point mCurrentPosition;

    private int[] mXpositions = new int[2];

    private int[] mYpositions = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        getActionBar().hide();
        final View dragView = findViewById(android.R.id.icon);
//        image.setOnClickListener(this);
        dragView.setTag(VIEW_TAG);
        dragView.setOnLongClickListener(this);
        findViewById(android.R.id.content).setOnDragListener(this);
        mCurrentPosition = new Point(0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case android.R.id.icon:
                Log.d(TAG, "user tap on icon button");
                onIconClick(v);
                break;
            default:
                Log.w(TAG, "onClick event not handle");
        }
    }

    private Point getNewPosition() {
        int x, y;
        Random r = new Random();
        do {
            x = mXpositions[r.nextInt(2)];
            y = mYpositions[r.nextInt(2)];
        } while (x == mCurrentPosition.x &&
                y == mCurrentPosition.y);
        return new Point(x, y);
    }

    private void checkMaxPositions(View view) {
        if (mXpositions[1] == 0 && mYpositions[1] == 0) {
            // Calculate max X and Y positions
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            mXpositions[1] = rect.width() - view.getWidth();
            mYpositions[1] = rect.height() - view.getHeight();
        }
    }

    private void animateToPoint(final View view, Point point) {
        view.animate()
                .alpha(0.5f)
                .x(mCurrentPosition.x)
                .y(mCurrentPosition.y)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setAlpha(1.0f);
                        view.setEnabled(true);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    private void onIconClick(final View view) {
        checkMaxPositions(view);
        mCurrentPosition = getNewPosition();
        animateToPoint(view, mCurrentPosition);
    }

    @Override
    public boolean onLongClick(View v) {
        checkMaxPositions(v);
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
        ClipData dragData = new ClipData(v.getTag().toString(), mimeTypes, item);

        // Instantiates the drag shadow builder.
        View.DragShadowBuilder myShadow = new MyDragShadowBuilder(v);
        v.startDrag(dragData, myShadow,
                v,
                0);
        return true;
    }

    // Base on http://developer.android.com/guide/topics/ui/drag-drop.html
    private static class MyDragShadowBuilder extends View.DragShadowBuilder {
        private static Drawable shadow;

        public MyDragShadowBuilder(View v) {
            super(v);
            Bitmap bitmap = getBitmapFromView(v);
            shadow = new BitmapDrawable(v.getResources(), bitmap);
        }

        private Bitmap getBitmapFromView(View v) {
            Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            v.draw(canvas);

            return bitmap;
        }

        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            int width, height;
            width = getView().getWidth();
            height = getView().getHeight();
            shadow.setBounds(0, 0, width, height);
            size.set(width, height);
            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {

        // Defines a variable to store the action type for the incoming event
        final int action = event.getAction();

        // Handles each of the expected events
        switch (action) {

            case DragEvent.ACTION_DRAG_STARTED:
                View draggedView = (View) event.getLocalState();
                draggedView.setVisibility(View.INVISIBLE);
                return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                return true;
            case DragEvent.ACTION_DROP:
                draggedView = (View) event.getLocalState();
                int eventX = (int) event.getX();
                int eventY = (int) event.getY();
                Point newPoint = null;
                if (eventX < OFF_SET && eventY < OFF_SET) {
                    //upper left
                    newPoint = new Point(0, 0);
                } else if (eventX > mXpositions[1] - OFF_SET &&
                        eventY < OFF_SET) {
                    // upper right
                    newPoint = new Point(mXpositions[1], 0);
                } else if (eventX < OFF_SET &&
                        eventY > mYpositions[1] - OFF_SET) {
                    // down left
                    newPoint = new Point(0, mYpositions[1]);
                } else if (eventX > mXpositions[1] - OFF_SET &&
                        eventY > mYpositions[1] - OFF_SET) {
                    // down right
                    newPoint = new Point(mXpositions[1], mYpositions[1]);
                }
                draggedView.setX(eventX);
                draggedView.setY(eventY);
                draggedView.setVisibility(View.VISIBLE);
                if (newPoint == null) {
                    // No closer to corner
                    newPoint = mCurrentPosition;
                }
                // In case newPoint != mCurrentPosition
                mCurrentPosition = newPoint;
                animateToPoint(draggedView, mCurrentPosition);
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                return true;
            // An unknown action type was received.
            default:
                Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                break;
        }

        return false;
    }

}
