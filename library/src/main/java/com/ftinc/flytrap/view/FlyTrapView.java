/*
 * Copyright (c) 2014 52inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ftinc.flytrap.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftinc.flytrap.R;
import com.ftinc.flytrap.model.Bug;
import com.ftinc.flytrap.model.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the custom view used to do all the mask rendering for
 * the function of this library
 *
 * Created by drew.heavner on 7/2/14.
 */
public class FlyTrapView extends RelativeLayout implements GestureDetector.OnGestureListener{
    private static final String TAG = FlyTrapView.class.getName();

    /***************************************************************************
     *
     * Variables
     *
     */

    private static final long ANIM_DURATION = 3000L;
    private static final long ACTION_ANIM_DURATION = 200L;

    private GestureDetector mGestureDetector;

    private List<Bug> mBugs;
    private Bug mActiveBug;

    private AnimatorSet mActiveSet;
    private float mActiveStartAngle = 0f;
    private float mActiveSweepAngle = 0f;

    private Paint mAccentPaint, mActivePaint;

    private LinearLayout mDoneLayout;

    private OnFlyTrapActionListener mActionListener;

    /***************************************************************************
     *
     * Constructors
     *
     */

    public FlyTrapView(Context context) {
        super(context);
        init();
    }

    public FlyTrapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlyTrapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Set the view's action listener
     *
     * @param listener      The flytrap action listener
     */
    public void setOnFlyTrapActionListener(OnFlyTrapActionListener listener){
        mActionListener = listener;
    }

    /***************************************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Initialize the view
     */
    private void init(){
        setWillNotDraw(false);

        // Create and add the 'Done' action to the view
        TextView doneView = new TextView(getContext());
        doneView.setText("Done");
        doneView.setTextColor(Color.WHITE);
        doneView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        doneView.setTypeface(null, Typeface.BOLD);
        doneView.setShadowLayer(2, 0, 1, Color.LTGRAY);

        ImageView nextImageView = new ImageView(getContext());
        nextImageView.setImageResource(R.drawable.ic_action_next);

        mDoneLayout = new LinearLayout(getContext());
        mDoneLayout.setBackgroundResource(R.drawable.done_selector);
        mDoneLayout.setOrientation(LinearLayout.HORIZONTAL);
        mDoneLayout.addView(doneView);
        mDoneLayout.addView(nextImageView);

        int padding = (int) dpToPx(getContext(), 16);
        mDoneLayout.setPadding(padding, padding, padding, padding);
        mDoneLayout.setGravity(Gravity.CENTER_VERTICAL);

        mDoneLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Done! Progressing to the next stage of FlyTrap");

                // Take Screenshots and package bug/comments/screenshots into compressed deliverable
                // and send it to the server.

                // Mask screen shot
                setDrawingCacheEnabled(true);
                Bitmap flyTrapMask = Bitmap.createBitmap(getDrawingCache());
                setDrawingCacheEnabled(false);

                // Generate screen of the originating activity

                Report report = new Report.Builder()
                        .addBugs(mBugs)
                        .build();


                // finish activity
                if(mActionListener != null) mActionListener.onDone(report);
            }
        });

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        addView(mDoneLayout, params);

        // Initialize gesture detector
        mGestureDetector = new GestureDetector(getContext(), this);

        // Initialize bug container
        mBugs = new ArrayList<>();

        // Setup the accent paint
        mAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAccentPaint.setStyle(Paint.Style.STROKE);
        mAccentPaint.setStrokeWidth(5);

        mActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mActivePaint.setStyle(Paint.Style.STROKE);
        mActivePaint.setStrokeWidth(5);
        mActivePaint.setColor(Color.CYAN);

    }

    /**
     * Add a new bug to the trap
     *
     * @param bug       the bug to add
     */
    private void addBug(Bug bug){
        Log.d(TAG, "Bug added [" + bug.getCenter() + "]");

        // Detect collision with other bugs and absorb those bugs into the new bug
        for(Bug b: mBugs){
            if(collision(bug, b)){
                // Absorb the existing bug into the new one, or vice versa
                float dist = distance(b.getCenter(), bug.getCenter());
                float newRadius = (dist + b.getRadius() + bug.getRadius()) / 2f;

                // Compute new center
                PointF newCenter = new PointF();
                float xDiff = b.getCenter().x - bug.getCenter().x;
                float yDiff = b.getCenter().y - bug.getCenter().y;
                newCenter.x = b.getCenter().x - (xDiff/2f);
                newCenter.y = b.getCenter().y - (yDiff/2f);

                // Update bug item
//                b.setCenter(newCenter);
//                b.setRadius(newRadius);

                // Animate absorbtion
                ObjectAnimator scale = ObjectAnimator.ofFloat(b, "radius", b.getRadius(), newRadius);
                scale.setDuration(ACTION_ANIM_DURATION);
                scale.setInterpolator(new AccelerateInterpolator());
                scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        invalidate();
                    }
                });

                ObjectAnimator transX = ObjectAnimator.ofFloat(b, "centerX", b.getCenterX(), newCenter.x);
                transX.setDuration(ACTION_ANIM_DURATION);
                transX.setInterpolator(new AccelerateInterpolator());

                ObjectAnimator transY = ObjectAnimator.ofFloat(b, "centerY", b.getCenterY(), newCenter.y);
                transY.setDuration(ACTION_ANIM_DURATION);
                transY.setInterpolator(new AccelerateInterpolator());

                AnimatorSet set = new AnimatorSet();
                set.playTogether(scale, transX, transY);
                set.start();

                // Refresh view
                invalidate();
                return;
            }
        }


        // Add bug to local store
        mBugs.add(bug);

        // Start animation of new bug object
        ObjectAnimator anim = ObjectAnimator.ofFloat(bug, "radius", 0, dpToPx(getContext(), 48));
        anim.setDuration(200L);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        anim.start();

        // Invalidate the view
        invalidate();
    }

    /**
     * Render all the bug stamp-outs onto the translucent shade
     */
    private Bitmap renderBugs(){

        Bitmap buffer = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(buffer);
        canvas.drawColor(getResources().getColor(R.color.black65));

        // Render all the bug elements on the canvas
        for(Bug bug: mBugs){
            bug.draw(canvas);
        }

        // Return the resulting bitmap
        return buffer;
    }

    /**
     * Render all the accents for the bug stamp-outs
     *
     * @param canvas    the canvas to render to
     */
    private void renderAccents(Canvas canvas){

        for(Bug bug: mBugs){

            // Configure paint
            mAccentPaint.setColor(bug.getAccentColor());

            // Print accent rings
            canvas.drawCircle(bug.getCenter().x, bug.getCenter().y, bug.getRadius(), mAccentPaint);

        }

    }

    /**
     * Render the current active bug if it exists
     *
     * @param canvas        the canvas to render to
     */
    private void renderActiveItem(Canvas canvas){
        if(mActiveBug != null){

            float radius = mActiveBug.getRadius() + 5;
            float x = mActiveBug.getCenter().x;
            float y = mActiveBug.getCenter().y;
            RectF oval = new RectF(x - radius, y - radius, x + radius, y + radius);

            canvas.drawArc(oval, mActiveStartAngle, mActiveSweepAngle, false, mActivePaint);

        }
    }

    /**
     * Find if a motion event hit an existing
     * bug, and return that bug if it did. Otherwise
     * return null.
     *
     * @param ev    the touch event
     * @return      the bug that was touched, null otherwise
     */
    private Bug didTouchBug(MotionEvent ev){

        float x = ev.getX();
        float y = ev.getY();

        for(Bug bug: mBugs){

            float dist = distance(new PointF(x, y), bug.getCenter());
            if(dist < bug.getRadius()){
                return bug;
            }

        }

        return null;
    }

    private void startActiveAnimations(){
        stopActiveAnimation();

        ValueAnimator startAngleAnim = ValueAnimator.ofFloat(0, 360);
        startAngleAnim.setDuration(ANIM_DURATION);
        startAngleAnim.setInterpolator(new LinearInterpolator());
        startAngleAnim.setRepeatCount(ValueAnimator.INFINITE);
        startAngleAnim.setRepeatMode(ValueAnimator.RESTART);
        startAngleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mActiveStartAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        ValueAnimator sweepAngleAnim = ValueAnimator.ofFloat(0, 360);
        sweepAngleAnim.setDuration(ANIM_DURATION);
        sweepAngleAnim.setInterpolator(new LinearInterpolator());
        sweepAngleAnim.setRepeatCount(ValueAnimator.INFINITE);
        sweepAngleAnim.setRepeatMode(ValueAnimator.REVERSE);
        sweepAngleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mActiveSweepAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mActiveSet = new AnimatorSet();
        mActiveSet.playTogether(startAngleAnim, sweepAngleAnim);
        mActiveSet.start();

    }

    private void stopActiveAnimation(){
        if(mActiveSet != null) {
            mActiveSet.end();
            mActiveSet = null;
        }

    }

    /***************************************************************************
     *
     * Override Methods
     *
     */


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "FlyTrap touched " + event);
        boolean result = mGestureDetector.onTouchEvent(event);

        // Detect drags to resize bugs


        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "FlyTrap intercept touch " + ev);

        Rect hitRect = new Rect();
        mDoneLayout.getHitRect(hitRect);
        if(hitRect.contains((int)ev.getX(), (int)ev.getY())){
            return false;
        }

        return true;
    }

    /**
     * Draw this view by masking out the circles from the bugs
     * out of a translucent black shade.
     *
     * Then render the accent animations/colors to give selected bug areas some
     * pop
     *
     * @param canvas    the canvas to render to
     */
    @Override
    protected void onDraw(Canvas canvas) {

        // Generate punched shade
        Bitmap buggedShade = renderBugs();

        // Draw the bitmap
        canvas.drawBitmap(buggedShade, 0, 0, null);

        // Render the bug accents
        renderAccents(canvas);

        // Render bug actions if active bug isn't null
        renderActiveItem(canvas);

        // Render Associating text


        super.onDraw(canvas);
    }


    /***************************************************************************
     *
     * Gesture Events
     *
     */


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        // Check to see if bug already exists
        if(didTouchBug(e) == null) {

            // Get the touch point
            PointF touch = new PointF(e.getX(), e.getY());

            // Generate the bug
            Bug bug = new Bug.Builder(0)
                    .setCenter(touch)
                    .setRadius(dpToPx(getContext(), 48))
                    .setAccentColor(getResources().getColor(android.R.color.holo_red_light))
                    .build();

            // Insert bug report into manager
            addBug(bug);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

        // Find collision of a bug
        Bug bug = didTouchBug(e);
        if(bug != null){
            // Check for existing active bug
            if(mActiveBug != null)
                mActiveBug = null;

            // Set the active bug
            mActiveBug = bug;

            // Log selection
            Log.d(TAG, "Bug selected: " + bug);

            // Animate in and display the bug actions
            startActiveAnimations();


        }else{
            mActiveBug = null;
            stopActiveAnimation();
        }

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /***************************************************************************
     *
     * Static Helper Methods
     *
     */

    /**
     * Compute the distance between two points
     *
     * @param p1		the first point
     * @param p2		the second point
     * @return			the distance between the two points
     */
    public static float distance(PointF p1, PointF p2){
        return (float) Math.sqrt(Math.pow((p2.x - p1.x), 2) + Math.pow(p2.y - p1.y,2));
    }

    /**
     * Return whether or not 2 bugs are colliding
     *
     * @param b1    bug 1
     * @param b2    bug 2
     * @return      true if colliding, false if not
     */
    public static boolean collision(Bug b1, Bug b2){
        float xDiff = b1.getCenter().x - b2.getCenter().x;
        float yDiff = b1.getCenter().y - b2.getCenter().y;
        float distSqr = xDiff*xDiff + yDiff*yDiff;
        return distSqr < (b1.getRadius() + b2.getRadius()) * (b1.getRadius() + b2.getRadius());
    }

    /**
     * Convert dp to px
     *
     * @param ctx
     * @param dp
     * @return
     */
    public static float dpToPx(Context ctx, float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
    }

    /***************************************************************************
     *
     * Interfaces
     *
     */

    /**
     * The FlyTrap view action listener that alerts listening
     * members of actions from within this view
     */
    public static interface OnFlyTrapActionListener{
        public void onDone(Report report);
    }

}
