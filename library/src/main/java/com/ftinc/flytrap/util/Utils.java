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

package com.ftinc.flytrap.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.ftinc.flytrap.model.Bug;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by drew.heavner on 7/9/14.
 */
public class Utils {

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

    /**
     * Compress a folder into the output
     *
     * @param folder        the input folder to compress the contents of
     * @param output        the output file to zip to
     * @return              true if operation was successful, false otherwise
     */
    public static boolean compress(File folder, File output){
        // Create zip output stream
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
            zipFolder(zos, folder);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Compress a folder into a zip archive
     *
     * @param out               the ZIP OutputStream
     * @param folder            the folder to compress
     * @throws IOException
     */
    private static void zipFolder(ZipOutputStream out, File folder) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin;

        for (File file : fileList) {
            if (file.isDirectory()) {
                zipFolder(out, file);
            } else {
                byte data[] = new byte[BUFFER];

                String unmodifiedFilePath = file.getPath();
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);

                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(file.getName());

                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }

                out.closeEntry();
                origin.close();
            }
        }
    }



    /**
     * Capture the root screenshot of a calling activity and store it
     * in a temporary file to later use
     *
     * @param activity      the calling activity
     * @return              the File object representation of the temporary image file stored
     */
    public static File captureRootScreenShot(Activity activity){
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
     * Check that in the system exists application which can handle this intent
     *
     * @param context Application context
     * @param intent  Checked intent
     * @return true if intent consumer exists, false otherwise
     */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

}
