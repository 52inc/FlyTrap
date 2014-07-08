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

        // Pull configuration
        Config config;
        if(getIntent() != null && getIntent().getExtras() != null){
            config = Config.createFromExtras(getIntent().getExtras());
        }else{
            throw new NullPointerException("You must provide a Config to the FlyTrap");
        }

        // Create FlyTrapView
        FlyTrapView view = new FlyTrapView(this, config);
        setContentView(view);

        view.setOnFlyTrapActionListener(new FlyTrapView.OnFlyTrapActionListener() {
            @Override
            public void onDone(Report report) {

                // Generate Report to disk
                report.generateReport();

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

            // Build default config
            Config config = Config.createDefault(ctx);
            config.rootImagePath = rootScreenShot.getPath();

            // Put the location of the temp file into the intent to be retrieved later
            config.apply(intent);

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
    public static void startFlyTrap(Activity ctx, Config config){
        // Capture screen from the calling activity and store in a temporary file for later use
        File rootScreenShot = captureRootScreenShot(ctx);
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

        // Create the screenshot per se
        Bitmap screenShot = Bitmap.createBitmap(decorBmp, 0, 0,  decorBmp.getWidth(), decorBmp.getHeight());

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
        public static final String ROOT_IMAGE_PATH = "root_image_path";

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

        /**
         * The root image path of the main nav
         */
        public String rootImagePath;

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
            intent.putExtra(ROOT_IMAGE_PATH, rootImagePath);
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
            config.defaultRadius = xtras.getFloat(DEFAULT_RADIUS);
            config.drawingCacheQuality = xtras.getInt(CACHE_QUALITY);
            config.rootImagePath = xtras.getString(ROOT_IMAGE_PATH);
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
            config.defaultRadius = FlyTrapView.dpToPx(ctx, 56);
            config.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH;
            return config;
        }

    }

}
