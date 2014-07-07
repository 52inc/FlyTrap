package com.ftinc.flytrap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.ftinc.flytrap.model.Report;
import com.ftinc.flytrap.view.FlyTrapView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by drew.heavner on 7/2/14.
 */
public class FlyTrap extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create FlyTrapView
        FlyTrapView view = new FlyTrapView(this);
        setContentView(view);

        view.setOnFlyTrapActionListener(new FlyTrapView.OnFlyTrapActionListener() {
            @Override
            public void onDone(Report report) {

                // Generate Report to disk


                // Handle generated report based on configuration


                finish();
            }
        });

    }


    /****************************************************************************************
     *
     * Static Methods
     *
     */

    /**
     * Start a default flytrack instance
     *
     * @param ctx
     */
    public static void startFlyTrap(Activity ctx){
        // Capture screen from the calling activity and store in a temporary file for later use
        File rootScreenShot = captureRootScreenShot(ctx);

        if(rootScreenShot != null) {

            // Generate intent to display flytrap activity
            Intent intent = new Intent(ctx, FlyTrap.class);

            // Put the location of the temp file into the intent to be retrieved later
            intent.putExtra("root_screen_shot", rootScreenShot.getPath());

            // Start FlyTrap
            ctx.startActivity(intent);
        }else{
            Toast.makeText(ctx, "Unable to capture screenshots, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start a flytrap instance with the supplied configuration
     *
     * @param ctx           the application context
     * @param config        the fly trap configuration
     */
    public static void startFlyTrap(Activity ctx, @NotNull("Must supply a configuration") Config config){
        // Capture screen from the calling activity and store in a temporary file for later use
        File rootScreenShot = captureRootScreenShot(ctx);

        if(rootScreenShot != null) {

            // Generate intent to display flytrap activity
            Intent intent = new Intent(ctx, FlyTrap.class);

            // Put the location of the temp file into the intent to be retrieved later
            intent.putExtra("root_screen_shot", rootScreenShot.getPath());

            // Input extras from configuration details
            config.apply(intent);

            // Start FlyTrap
            ctx.startActivity(intent);
        }else{
            Toast.makeText(ctx, "Unable to capture screenshots, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Capture the root screenshot of a calling activity and store it
     * in a temporary file to later use
     *
     * @param activity      the calling activity
     * @return              the File object representation of the temporary image file stored
     */
    private static File captureRootScreenShot(Activity activity){
        View decor = activity.getWindow().getDecorView();
        decor.setDrawingCacheEnabled(true);

        // Configure screenshot bounds
        Bitmap decorBmp = decor.getDrawingCache();
        int contentViewTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int contentViewBottom = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getBottom();

        // Create the screenshot per se
        Bitmap screenShot = Bitmap.createBitmap(decorBmp, 0, contentViewTop,  decorBmp.getWidth(), contentViewBottom - contentViewTop);

        // Recycle the intial bitmap
        decorBmp.recycle();

        // Disable drawing cache on the decor
        decor.setDrawingCacheEnabled(false);

        // Save the newly generated screenshot into a temporary variable
        try {

            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "PNG_" + timeStamp + "_";
            File cacheDir = activity.getCacheDir();
            File tempFile = File.createTempFile(imageFileName, ".png", cacheDir);

            // Write bitmap to file
            boolean result = screenShot.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(tempFile));
            if(result)
                return tempFile;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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
        public static final String DEFAULT_RADIUS = "default_radius";
        public static final String CACHE_QUALITY = "drawing_cache_quality";

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
         * This configures the default radius of new bug items added to the
         * fly trap view
         */
        public float defaultRadius;

        /**
         * This configures the quality of the screenshots taken
         */
        public int drawingCacheQuality;

        /*
         *
         * Configurations that deal with uploading the report to the
         * developer.
         *
         */


        /**
         * Empty Constructor
         */
        public Config(){}

        /**
         * Apply this configuration to an Intent meant to launch
         * the FlyTrap instance
         *
         * @param intent        the intent to launch the FlyTrap activity
         */
        public void apply(Intent intent){

            intent.putExtra(ACCENT_COLOR, accentColor);
            intent.putExtra(DEFAULT_RADIUS, defaultRadius);
            intent.putExtra(CACHE_QUALITY, drawingCacheQuality);

        }

    }

}
