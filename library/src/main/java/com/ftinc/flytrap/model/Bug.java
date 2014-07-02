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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    public static final String KEY_COMMENTS = "comments";


    /***************************************************************************
     *
     * Variables
     *
     */

    private int id;
    private PointF center;
    private float radius;
    private int accentColor;
    private List<Comment> comments;

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
        comments = new ArrayList<>();
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
    public int getId(){ return id; }

    /**
     * Get the center target location of this bug on the
     * screen.
     */
    public PointF getCenter(){ return center; }

    /**
     * Get the radius of the bug punchout on the view.
     */
    public float getRadius(){ return radius; }

    /**
     * Get the accent color of this bug
     */
    public int getAccentColor(){ return accentColor; }

    /**
     * Add a comment to this bug
     *
     * @param comment       the comment to add
     */
    public void addComment(Comment comment){
        comments.add(comment);
    }

    /**
     * Remove a comment from this bug
     *
     * @param comment       the comment to remove
     */
    public void removeComment(Comment comment){
        comments.remove(comment);
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
        this.comments = jsonToComments(json.optJSONArray(KEY_COMMENTS));
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
     * Convert a list of comments to a JSONArray
     *
     * @param comments      the list of comments to convert
     * @return              the JSONArray format of comments
     */
    private static JSONArray commentsToJson(List<Comment> comments){
        JSONArray array = new JSONArray();

        for(Comment comment: comments){
            array.put(comment.toString());
        }

        return array;
    }

    /**
     * Convert a JSONArray into a list of Comments
     *
     * @param json      the json array to convert
     * @return          the list of converted comments
     */
    private static List<Comment> jsonToComments(JSONArray json){
        List<Comment> comments = new ArrayList<>();
        for(int i=0; i<json.length(); i++){
            String comment = json.optString(i, null);
            if(comment != null){
                Comment cmt = new Comment(comment);
                comments.add(cmt);
            }
        }
        return comments;
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
            json.put(KEY_COLOR, accentColor);
            json.put(KEY_COMMENTS, commentsToJson(comments));
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
