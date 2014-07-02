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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.ftinc.flytrap.R;
import com.ftinc.flytrap.model.Bug;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the custom view used to do all the mask rendering for
 * the function of this library
 *
 * Created by drew.heavner on 7/2/14.
 */
public class FlyTrapView extends RelativeLayout implements GestureDetector.OnGestureListener{

    /***************************************************************************
     *
     * Variables
     *
     */

    private GestureDetector mGestureDetector;

    private List<Bug> mBugs;

    private Paint mAccentPaint;

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

        // Initialize gesture detector
        mGestureDetector = new GestureDetector(getContext(), this);

        // Initialize bug container
        mBugs = new ArrayList<>();

        // Setup the accent paint
        mAccentPaint = new Paint();
        mAccentPaint.setStyle(Paint.Style.STROKE);

    }

    /**
     * Add a new bug to the trap
     *
     * @param bug       the bug to add
     */
    private void addBug(Bug bug){
        // Add bug to local store
        mBugs.add(bug);

        // Add the appropriate UI for a new bug

        // Start animation of new bug object

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

        }

    }

    /***************************************************************************
     *
     * Override Methods
     *
     */


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
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

        // Get the touch point
        PointF touch = new PointF(e.getX(), e.getY());

        // Generate the bug
        Bug bug = new Bug.Builder(0)
                .setCenter(touch)
                .setRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics()))
                .setAccentColor(getResources().getColor(android.R.color.holo_blue_light))
                .build();

        // Insert bug report into manager
        addBug(bug);

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /***************************************************************************
     *
     * Interfaces
     *
     */

}
