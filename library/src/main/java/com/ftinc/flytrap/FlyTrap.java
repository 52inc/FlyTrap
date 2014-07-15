/*
 * Copyright (c) 2014 52inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ftinc.flytrap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ftinc.flytrap.model.Delivery;
import com.ftinc.flytrap.model.EmailDelivery;
import com.ftinc.flytrap.model.Report;
import com.ftinc.flytrap.util.Utils;
import com.ftinc.flytrap.view.FlyTrapView;

import java.io.File;

/**
 * Created by drew.heavner on 7/2/14.
 */
public class FlyTrap extends Activity {

    /*
     * Configuration variable
     */
    private Config mConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pull configuration
        if(getIntent() != null && getIntent().getExtras() != null){
            mConfig = Config.createFromExtras(getIntent().getExtras());
        }

        // Make sure a configuration was loaded
        if(mConfig == null)
            throw new NullPointerException("You must provide a Config to the FlyTrap");

        // Create FlyTrapView
        FlyTrapView view = new FlyTrapView(this, mConfig);
        setContentView(view);

        /*
         * Setup the action listener that is called when the user presses the done button
         * to indicate that they have finished creating the feedback report
         */
        view.setOnFlyTrapActionListener(new FlyTrapView.OnFlyTrapActionListener() {
            @Override
            public void onDone(Report report) {
                if(mConfig.deliverySystem != null){
                    mConfig.deliverySystem.onReportGenerated(FlyTrap.this, report, new Delivery.OnReportHandler() {
                        @Override
                        public void onFinish() {
                            finish();
                        }
                    });
                }
            }
        });

    }


    /****************************************************************************************
     *
     * Static Methods
     *
     */

    /**
     * Start a default FlyTrap instance with a default configuration that will just have the user
     * email the configuration to you
     *
     * @param ctx   the Activity context used to launch the activity
     */
    public static void startFlyTrap(Activity ctx, String developerEmailAddress){
        // Capture screen from the calling activity and store in a temporary file for later use
        File rootScreenShot = Utils.captureRootScreenShot(ctx);

        if(rootScreenShot != null) {

            // Generate intent to display flytrap activity
            Intent intent = new Intent(ctx, FlyTrap.class);

            // Build default config
            Config config = Config.createDefault(ctx);
            config.rootImagePath = rootScreenShot.getPath();
            config.deliverySystem = new EmailDelivery(developerEmailAddress, "App Feedback", "");

            // Put the location of the temp file into the intent to be retrieved later
            config.apply(intent);

            // Start FlyTrap
            ctx.startActivity(intent);
        }else{
            Toast.makeText(ctx, "Unable to capture screenshots, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start a FlyTrap instance with a supplied configuration
     *
     * @param ctx           the application context
     * @param config        the fly trap configuration
     */
    public static void startFlyTrap(Activity ctx, Config config){
        // Capture screen from the calling activity and store in a temporary file for later use
        File rootScreenShot = Utils.captureRootScreenShot(ctx);
        if(rootScreenShot != null) {

            // Generate intent to display flytrap activity
            Intent intent = new Intent(ctx, FlyTrap.class);

            // Input extras from configuration details
            config.rootImagePath = rootScreenShot.getPath();
            config.apply(intent);

            // Start FlyTrap
            ctx.startActivity(intent);
        }else{
            Toast.makeText(ctx, "Unable to capture screenshots, please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * FlyTrap configurations for creating fly trap screens to capture
     * feedback from the user.
     *
     */
    public static class Config{

        /******************************************
         *
         * Config Constants
         *
         */

        public static final String ACCENT_COLOR = "accent_color";
        public static final String ACTIVE_COLOR = "active_color";
        public static final String DEFAULT_RADIUS = "default_radius";
        public static final String CACHE_QUALITY = "drawing_cache_quality";
        public static final String ROOT_IMAGE_PATH = "root_image_path";
        public static final String DELIVERY_SYSTEM = "delivery_system";

        /******************************************
         *
         * Config Variables
         *
         */

        /**
         * This configures the accent color of the FlyTrap view
         * and its items.
         */
        public int accentColor;

        /**
         * This configures the activated color of bug items when they are selected
         * and activated
         */
        public int activeColor;

        /**
         * This configures the default radius of new bug items added to the
         * fly trap view
         */
        public float defaultRadius;

        /**
         * This configures the quality of the screenshots taken
         */
        public int drawingCacheQuality;

        /**
         * The root image path of the main nav
         */
        public String rootImagePath;

        /**
         * This is the delivery configuration that will handle sending the feedback reports
         * to the developer
         */
        public Delivery deliverySystem;

        /**
         * Empty Constructor
         */
        public Config(){}

        /******************************************
         *
         * Helper Methods
         *
         */

        /**
         * Apply this configuration to an Intent meant to launch
         * the FlyTrap instance
         *
         * @param intent        the intent to launch the FlyTrap activity
         */
        public void apply(Intent intent){
            intent.putExtra(ACCENT_COLOR, accentColor);
            intent.putExtra(ACTIVE_COLOR, activeColor);
            intent.putExtra(DEFAULT_RADIUS, defaultRadius);
            intent.putExtra(CACHE_QUALITY, drawingCacheQuality);
            intent.putExtra(ROOT_IMAGE_PATH, rootImagePath);
            intent.putExtra(DELIVERY_SYSTEM, deliverySystem);
        }

        /**
         * Restore an instance of config from Bundle extras in an activity
         * after being launched by a launcher method
         *
         * @param xtras
         * @return
         */
        public static Config createFromExtras(Bundle xtras){
            Config config = new Config();
            config.accentColor = xtras.getInt(ACCENT_COLOR);
            config.activeColor = xtras.getInt(ACTIVE_COLOR);
            config.defaultRadius = xtras.getFloat(DEFAULT_RADIUS);
            config.drawingCacheQuality = xtras.getInt(CACHE_QUALITY);
            config.rootImagePath = xtras.getString(ROOT_IMAGE_PATH);
            config.deliverySystem = xtras.getParcelable(DELIVERY_SYSTEM);
            return config;
        }

        /**
         * Create a default configuration
         *
         * @param ctx   the application context
         * @return      the default configuration
         */
        public static Config createDefault(Context ctx){
            Config config = new Config();
            config.accentColor = ctx.getResources().getColor(android.R.color.holo_blue_light);
            config.defaultRadius = Utils.dpToPx(ctx, 56);
            config.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH;
            return config;
        }

        /******************************************
         *
         * Builder Class
         *
         */

        /**
         * The builder class that provides an easy way to construct
         * FlyTrap configurations
         *
         */
        public static class Builder{

            // The object being built
            private Config config;

            /**
             * Create a new instance of the Config.Builder
             * to construct a configuration for FlyTrap
             */
            public Builder(){
                config = new Config();
            }

            /******************************************
             *
             * Build Methods
             *
             */

            /**
             * Set the accent color used to accent the bug punchouts
             *
             * @param color     the color to use
             * @return          self for chaining
             */
            public Builder setAccentColor(int color){
                config.accentColor = color;
                return this;
            }

            /**
             * Set teh active color to use when the user selects one of the bug
             * annotations.
             *
             * @param color     the active color to use
             * @return          self for chaining
             */
            public Builder setActiveColor(int color){
                config.activeColor = color;
                return this;
            }

            /**
             * Set the default radius of the bug feedbacks when the user taps a location on the
             * feedback shade.
             *
             * @param radius        the radius of the feedback punch hole
             * @return              self for chaining
             */
            public Builder setRadius(float radius){
                config.defaultRadius = radius;
                return this;
            }

            /**
             * Set the quality of the screenshots taken from the application
             * and feedback shade.
             *
             * @param quality   {@link View#DRAWING_CACHE_QUALITY_LOW} or {@link View#DRAWING_CACHE_QUALITY_HIGH} or {@link View#DRAWING_CACHE_QUALITY_AUTO}
             * @return          self for chaining
             */
            public Builder setScreenshotQuality(int quality){
                config.drawingCacheQuality = quality;
                return this;
            }

            /**
             * Set the delivery system to use once the user has completed entering feedback
             *
             * @param deliverySystem        the delivery system to use
             * @return                      self for chaining
             */
            public Builder setDeliverySystem(Delivery deliverySystem){
                config.deliverySystem = deliverySystem;
                return this;
            }

            /**
             * Build and return the configuration for FlyTrap
             *
             * @return      the FlyTrap configuration
             */
            public Config build(){
                return config;
            }


        }


    }

}
