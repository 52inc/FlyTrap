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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftinc.flytrap.FlyTrap;
import com.ftinc.flytrap.R;
import com.ftinc.flytrap.model.Bug;
import com.ftinc.flytrap.model.Report;
import com.ftinc.flytrap.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the custom view used to do all the mask rendering for
 * the function of this library
 *
 * Created by drew.heavner on 7/2/14.
 */
public class FlyTrapView extends RelativeLayout implements GestureDetector.OnGestureListener, View.OnClickListener {
    private static final String TAG = FlyTrapView.class.getName();

    /***************************************************************************
     *
     * Constants
     *
     */

    private static final long ANIM_DURATION = 3000L;
    private static final long ACTION_ANIM_DURATION = 200L;
    private static final long SHEET_ANIM_DURATION = 300L;
    private static final float BOTTOMSHEET_HEIGHT = 250f; // 250dp

    /***************************************************************************
     *
     * Variables
     *
     */

    private GestureDetector mGestureDetector;
    private InputMethodManager mImm;

    private List<Bug> mBugs;
    private Bug mActiveBug;

    private AnimatorSet mActiveSet;
    private float mActiveStartAngle = 0f;
    private float mActiveSweepAngle = 0f;

    private Paint mAccentPaint, mActivePaint;

    private LinearLayout mDoneLayout;
    private LinearLayout mCommentSheet;
    private EditText mCommentField;
    private TextView mCommentDone;

    private OnFlyTrapActionListener mActionListener;
    private FlyTrap.Config mConfig;

    /***************************************************************************
     *
     * Constructors
     *
     */

    /**
     * Config Constructor
     *
     * @param context   the application context
     * @param config    the flytrap config
     */
    public FlyTrapView(Context context, FlyTrap.Config config) {
        super(context);
        mConfig = config;
        init();
    }

//    public FlyTrapView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init();
//    }
//
//    public FlyTrapView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init();
//    }

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
        // Enable custom viewgroup drawing
        setWillNotDraw(false);

        // Initialize gesture detector
        mGestureDetector = new GestureDetector(getContext(), this);

        // Get the input method manager
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Setup the done text view for the done button
        TextView doneView = new TextView(getContext());
        doneView.setText("Done");
        doneView.setTextColor(Color.WHITE);
        doneView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        doneView.setTypeface(null, Typeface.BOLD);
        doneView.setShadowLayer(2, 0, 1, Color.LTGRAY);
        doneView.setGravity(Gravity.CENTER);

        // Setup the forward arrow for the done button
        ImageView nextImageView = new ImageView(getContext());
        nextImageView.setImageResource(R.drawable.ic_action_next);

        // Setup the Done button for the user to indicate that they have finished creating their feedback report
        mDoneLayout = new LinearLayout(getContext());
        mDoneLayout.setBackgroundResource(R.drawable.done_selector);
        mDoneLayout.setOrientation(LinearLayout.HORIZONTAL);
        mDoneLayout.addView(doneView);
        mDoneLayout.addView(nextImageView);
        int padding = (int) Utils.dpToPx(getContext(), 16);
        mDoneLayout.setPadding(padding, padding, padding, padding);
        mDoneLayout.setGravity(Gravity.CENTER_VERTICAL);
        mDoneLayout.setOnClickListener(this);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        addView(mDoneLayout, params);

        // Setup the bottom sheet comment entry panel
        mCommentSheet = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_comment_bottomsheet, null, false);
        mCommentSheet.setVisibility(View.GONE);
        mCommentField = (EditText) mCommentSheet.findViewById(R.id.comment_field);
        mCommentDone = (TextView) mCommentSheet.findViewById(R.id.action_done);
        LayoutParams commentSheetParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commentSheetParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(mCommentSheet, commentSheetParams);

        // Initialize bug container
        mBugs = new ArrayList<>();

        // Define the accent paint style
        mAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAccentPaint.setStyle(Paint.Style.STROKE);
        mAccentPaint.setStrokeWidth(5);

        // Define the active state paint object
        mActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mActivePaint.setStyle(Paint.Style.STROKE);
        mActivePaint.setStrokeWidth(6.5f);
        mActivePaint.setColor(Color.CYAN);

    }

    /**
     * Add a new bug to the trap
     *
     * @param bug       the bug to add
     */
    private void addBug(Bug bug){
        if(!collideAndAbsorb(bug)) {

            // Add bug to local store
            mBugs.add(bug);

            // Start animation of new bug object
            ObjectAnimator anim = ObjectAnimator.ofFloat(bug, "radius", 0, mConfig.defaultRadius);
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

            float radius = mActiveBug.getRadius(); // + 5;
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
            float dist = Utils.distance(new PointF(x, y), bug.getCenter());
            if(dist < bug.getRadius()){
                return bug;
            }
        }

        return null;
    }

    /**
     * Start the active bug animation
     */
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

    /**
     * Stop the active animation
     */
    private void stopActiveAnimation(){
        if(mActiveSet != null) {
            mActiveSet.end();
            mActiveSet = null;
        }

    }

    /**
     * Check collision of a bug with the rest of the existing bugs and if a collision exists
     * return true and then continue to chain absorption until collisions cease to exist
     *
     * @param bug   the bug to check for collisions for
     * @return      true if collision exists, false if no collisions exist
     */
    public boolean collideAndAbsorb(final Bug bug){
        if(mBugs.contains(bug)) mBugs.remove(bug);

        // Detect collision with other bugs and absorb those bugs into the new bug
        for(final Bug b: mBugs){
            if(Utils.collision(bug, b)){
                // Absorb the existing bug into the new one, or vice versa
                float newRadius = (b.getRadius() + bug.getRadius()/2f);

                // Compute new center
                PointF newCenter = new PointF();
                float xDiff = b.getCenter().x - bug.getCenter().x;
                float yDiff = b.getCenter().y - bug.getCenter().y;
                newCenter.x = b.getCenter().x - (xDiff/2f);
                newCenter.y = b.getCenter().y - (yDiff/2f);

                // Animate absorbtion
                ObjectAnimator scale = ObjectAnimator.ofFloat(b, "radius", b.getRadius(), newRadius);
                scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        invalidate();
                    }
                });

                ObjectAnimator transX = ObjectAnimator.ofFloat(b, "centerX", b.getCenterX(), newCenter.x);
                ObjectAnimator transY = ObjectAnimator.ofFloat(b, "centerY", b.getCenterY(), newCenter.y);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(scale, transX, transY);
                set.setDuration(ACTION_ANIM_DURATION);
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        for(int i=0; i<mBugs.size(); i++) {
                            Bug b2 = mBugs.get(i);
                            if(Utils.collision(b, b2) && b != b2) {
                                collideAndAbsorb(b2);
                                break;
                            }
                        }

                    }
                });
                set.start();

                invalidate();
                return true;
            }
        }

        return false;
    }

    /**
     * Show the comment bottom sheet
     */
    private void showCommentBottomSheet(){
        float height = Utils.dpToPx(getContext(), BOTTOMSHEET_HEIGHT);

        if(mCommentSheet.getVisibility() == View.GONE) {

            // Create animator to slide sheet up from the bottom
            ObjectAnimator transY = ObjectAnimator.ofFloat(mCommentSheet, "translationY", height, 0);
            transY.setDuration(SHEET_ANIM_DURATION);
            transY.setInterpolator(new DecelerateInterpolator());
            transY.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mCommentSheet.setVisibility(View.VISIBLE);

                    // Add new action listeners for the bottom sheet's views
                    mCommentField.setText(mActiveBug.getComment());
                    mCommentField.setSelection(mActiveBug.getComment().length());
                    mCommentDone.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Update the active bug's comment text with what was entered
                            String comment = mCommentField.getText().toString();
                            mActiveBug.setComment(comment);

                            // End the active mode
                            mActiveBug = null;
                            stopActiveAnimation();
                            hideCommentBottomSheet();

                            // Hide the keyboard
                            mImm.hideSoftInputFromWindow(mCommentDone.getWindowToken(), 0);
                        }
                    });

                }
            });

            transY.start();

        }else{

            // Add new action listeners for the bottom sheet's views
            mCommentField.setText(mActiveBug.getComment());
            mCommentDone.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Update the active bug's comment text with what was entered
                    String comment = mCommentField.getText().toString();
                    mCommentField.setSelection(mActiveBug.getComment().length());
                    mActiveBug.setComment(comment);

                    // End the active mode
                    mActiveBug = null;
                    stopActiveAnimation();
                    hideCommentBottomSheet();

                    // Hide the keyboard
                    mImm.hideSoftInputFromWindow(mCommentDone.getWindowToken(), 0);
                }
            });

        }

    }

    /**
     * Hide the comment bottom sheet
     */
    private void hideCommentBottomSheet(){
        float height = Utils.dpToPx(getContext(), BOTTOMSHEET_HEIGHT);

        // Create animator to slide the sheet down off the screen
        ObjectAnimator transY = ObjectAnimator.ofFloat(mCommentSheet, "translationY", 0, height);
        transY.setDuration(SHEET_ANIM_DURATION);
        transY.setInterpolator(new AccelerateInterpolator());
        transY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCommentSheet.setVisibility(View.GONE);
            }
        });

        transY.start();
    }

    /***************************************************************************
     *
     * Override Methods
     *
     */

    /*
     *
     * The Touch Variables
     *
     */

    private Bug mSelectedBug;
    private PointF mLastPos;
    private PointF mStartPos;
    private float mStartRadius;
    private boolean mIsScaleMode = false;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mGestureDetector.onTouchEvent(event);

        // Detect drags to resize bugs
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:

                // Check to see if the down state was in a bug object
                for(Bug bug: mBugs){
                    if(bug.collidesWith(event.getX(), event.getY())){
                        if(mActiveBug != null && mActiveBug.getId() != bug.getId())
                            continue;

                        mIsScaleMode = mActiveBug != null ? mActiveBug.getId() == bug.getId() : false;
                        mSelectedBug = bug;
                        mLastPos = mStartPos = new PointF(event.getX(), event.getY());
                        mStartRadius = bug.getRadius();
                        return true;

                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if(mSelectedBug != null){

                    if(mIsScaleMode) {

                        // Compute distance from center
                        float dY = event.getY() - mStartPos.y;

                        // Apply this as the new radius
                        mSelectedBug.setRadius(mStartRadius + dY);

                        invalidate();
                        return true;
                    }else{

                        // Calculate delta change from last point
                        float dX = event.getX() - mLastPos.x;
                        float dY = event.getY() - mLastPos.y;

                        // move bug by changed amount
                        mSelectedBug.setCenterX(mSelectedBug.getCenterX() + dX);
                        mSelectedBug.setCenterY(mSelectedBug.getCenterY() + dY);
                        invalidate();

                        mLastPos.set(event.getX(), event.getY());
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                mSelectedBug = null;

                break;
            case MotionEvent.ACTION_CANCEL:
                mSelectedBug = null;

                break;
        }


        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        Rect hitRect = new Rect();
        mDoneLayout.getHitRect(hitRect);
        if(hitRect.contains((int)ev.getX(), (int)ev.getY()) || mActiveBug != null){
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
        Bug touched = didTouchBug(e);
        if(touched == null) {

            if(mActiveBug == null) {

                // Get the touch point
                PointF touch = new PointF(e.getX(), e.getY());

                // Generate the bug
                Bug bug = new Bug.Builder(mBugs.size())
                        .setCenter(touch)
                        .setRadius(mConfig.defaultRadius)
                        .setAccentColor(mConfig.accentColor)
                        .build();

                // Insert bug report into manager
                addBug(bug);

            }else{

                // Update the active bug's comment text with what was entered
                String comment = mCommentField.getText().toString();
                mActiveBug.setComment(comment);

                // End the active mode
                mActiveBug = null;
                stopActiveAnimation();
                hideCommentBottomSheet();

                // Hide the keyboard
                mImm.hideSoftInputFromWindow(mCommentDone.getWindowToken(), 0);
            }

            return true;
        }else{
            // Start active mode for this bug
            // Check for existing active bug
            if(mActiveBug != null && mActiveBug.getId() == touched.getId()){

                // De-activate active mode
                String comment = mCommentField.getText().toString();
                mActiveBug.setComment(comment);

                // End the active mode
                mActiveBug = null;
                stopActiveAnimation();
                hideCommentBottomSheet();

                // Hide the keyboard
                mImm.hideSoftInputFromWindow(mCommentDone.getWindowToken(), 0);

            }else {

                // If active bug isn't null, save the comment entry
                if(mActiveBug != null){
                    mActiveBug.setComment(mCommentField.getText().toString());
                }

                // Set the active bug
                mActiveBug = touched;

                // Log selection
                Log.d(TAG, "Bug activated: " + mActiveBug.getId());

                // Animate in and display the bug actions
                startActiveAnimations();

                // Show the comment bottom sheet
                showCommentBottomSheet();
            }

            return true;
        }
        //return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * Handle the user pressing the done button to indicate that they have finished creating a
     * feedback report
     *
     * @param v     the view clicked
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "Done! Progressing to the next stage of FlyTrap");

        // Mask screen shot
        setDrawingCacheEnabled(true);
        Bitmap flyTrapMask = Bitmap.createBitmap(getDrawingCache());
        setDrawingCacheEnabled(false);

        // Merge this bitmap with the root bitmap to form the image the user looks at


        // Save the newly generated screenshot into a temporary variable
        try {

            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "PNG_" + timeStamp + "_";
            File cacheDir = getContext().getCacheDir();
            File tempFile = File.createTempFile(imageFileName, ".png", cacheDir);

            // Write bitmap to file
            boolean result = flyTrapMask.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(tempFile));
            if(result){

                // Generate screen of the originating activity
                Report report = new Report.Builder()
                        .addBugs(mBugs)
                        .setBaseScreenshot(mConfig.rootImagePath)
                        .setShadeScreenshot(tempFile.getPath())
                        .build();

                // finish activity
                if(mActionListener != null) mActionListener.onDone(report);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
