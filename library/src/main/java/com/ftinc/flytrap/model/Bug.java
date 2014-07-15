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

package com.ftinc.flytrap.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.ftinc.flytrap.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is a single bug instance on the FlyTrap view that indicates where
 * the user tapped to indicate a bug. This gives locational data to the where
 * it is on the screen, and gives layout/UI data on how to represent this bug
 * on the fly trap shade.
 *
 * This class will also contain attachments from the user such as a debug log or
 * a user comment about the bug.
 *
 * @format  JSON
 *
 * Created by drew.heavner on 7/2/14.
 */
public class Bug {

    /***************************************************************************
     *
     * Static Methods
     *
     */

    /**
     * Create a 'Bug' object from a json map
     *
     * @param json      the json representation of a Bug
     * @return          the inflated Bug object
     */
    public static Bug fromJson(JSONObject json){
        Bug bug = new Bug();
        bug.inflateJson(json);
        return bug;
    }

    /***************************************************************************
     *
     * Constants
     *
     */

    /* JSON Keys */
    public static final String KEY_ID = "id";
    public static final String KEY_CENTER = "center";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_COLOR = "accent_color";
    public static final String KEY_COMMENT = "comment";


    /***************************************************************************
     *
     * Variables
     *
     */

    private int id;
    private PointF center;
    private float radius;
    private int accentColor;
    private String comment = "";

    private Paint paint;

    /**
     * Hidden Empty Constructor
     */
    private Bug(){
        init();
    }

    /**
     * Hidden constructor
     */
    private Bug(int id){
        init();
        this.id = id;
        center = new PointF();
    }

    /**
     * Initialize this bug
     */
    private void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setStyle(Paint.Style.FILL);
    }


    /***************************************************************************
     *
     * Accessor Methods
     *
     */


    /**
     * Get the id of this bug
     */
    public int getId(){
        return id;
    }

    /**
     * Get the center target location of this bug on the
     * screen.
     */
    public PointF getCenter(){
        return center;
    }

    /**
     * Update the center location of this bug item
     * @param center
     */
    public void setCenter(PointF center){
        this.center = center;
    }

    /**
     * Get the center position's x-coordinate
     */
    public float getCenterX(){
        return center.x;
    }

    /**
     * Set the center position's x-coordinate
     */
    public void setCenterX(float val){
        center.x = val;
    }

    /**
     * Get the center position's y-coordinate
     */
    public float getCenterY(){
        return center.y;
    }

    /**
     * Set the center position's y-coordinate
     * @param val
     */
    public void setCenterY(float val){
        center.y = val;
    }

    /**
     * Get the radius of the bug punchout on the view.
     */
    public float getRadius(){
        return radius;
    }

    /**
     * Update the radius of this bug item
     * @param radius        the new radius
     */
    public void setRadius(float radius){
        this.radius = radius;
    }

    /**
     * Get the accent color of this bug
     */
    public int getAccentColor(){
        return accentColor;
    }

    /**
     * Set the accent color of this bug
     *
     * @param color     the accent color to set
     */
    public void setAccentColor(int color){
        this.accentColor = color;
    }

    /**
     * Set the text comment on this bug
     *
     * @param comment       the comment to set
     */
    public void setComment(String comment){
        this.comment = comment;
    }

    /**
     * Get the text comment on this bug
     *
     * @return
     */
    public String getComment(){
        return this.comment;
    }

    /***************************************************************************
     *
     * Helper Methods
     *
     */


    /**
     * Inflate a json object to fill out this class
     *
     * @param json      the json to inflate from
     */
    private void inflateJson(JSONObject json){
        this.id = json.optInt(KEY_ID);
        this.center = jsonToPoint(json.optJSONObject(KEY_CENTER));
        this.radius = (float) json.optDouble(KEY_RADIUS);
        this.accentColor = json.optInt(KEY_COLOR);
        this.comment = json.optString(KEY_COMMENT);
    }

    /**
     * Convert a PointF object into JSONObject
     *
     * @param point     the point to convert
     * @return          the json formatted point
     *
     * @throws JSONException    error mapping values to json
     */
    private static JSONObject pointToJson(PointF point) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("x", point.x);
        json.put("y", point.y);
        return json;
    }

    /**
     * Convert a JSONObject back into a PointF object
     *
     * @param json      the json to convert from
     * @return          the PointF object
     */
    private static PointF jsonToPoint(JSONObject json){
        PointF point = new PointF();
        point.x = (float) json.optDouble("x", 0);
        point.y = (float) json.optDouble("y", 0);
        return point;
    }

    /**
     * Check if point in space collides with this bug
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @return      true if collides, false otherwise
     */
    public boolean collidesWith(float x, float y){
        return Utils.distance(new PointF(x, y), center) < radius;
    }

    /***************************************************************************
     *
     * Public Methods
     *
     */


    /**
     * Get a JSON representation of this class
     *
     * @return      this Bug formatted in JSON
     */
    public JSONObject toJSON(){
        JSONObject json = new JSONObject();

        try {
            json.put(KEY_ID, id);
            json.put(KEY_CENTER, pointToJson(center));
            json.put(KEY_RADIUS, radius);
            json.put(KEY_COLOR, String.format("#%06X", (0xFFFFFF & accentColor)));
            json.put(KEY_COMMENT, comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    /**
     * Render this bug on the canvas
     *
     * @param canvas        the canvas to render this bug to
     */
    public void draw(Canvas canvas){
        canvas.drawCircle(center.x, center.y, radius, paint);
    }


    /***************************************************************************
     *
     * Builder
     *
     */

    /**
     * This is the helper class to construct new bugs
     * for the flytrap view
     *
     */
    public static class Builder{

        // The object to build
        private Bug bug;

        /**
         * Create a new builder to construct a bug
         *
         * @param id    the id # for this bug
         */
        public Builder(int id){
            bug = new Bug(id);
        }

        /**
         * Set the center point of the bug the user has requested be
         * generated.
         *
         * @param position      the center location of this item
         * @return              self for chaining
         */
        public Builder setCenter(PointF position){
            bug.center = position;
            return this;
        }

        /**
         * Set the radius of the circle punchout for highlighting bug
         * areas on the view.
         *
         * @param radius    the radius of the circle
         * @return          self for chaining
         */
        public Builder setRadius(float radius){
            bug.radius = radius;
            return this;
        }

        /**
         * Set the accent color that will be used to help highlight the
         * new bug selection to the user
         *
         * @param color     the highlight color
         * @return          self for chaining
         */
        public Builder setAccentColor(int color){
            bug.accentColor = color;
            return this;
        }

        /**
         * Build the Bug object
         *
         * @return      the compiled bug reference
         */
        public Bug build(){
            return bug;
        }

    }

}
