package com.ftinc.flytrap.model;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * This is the collective bug report object that will handle all the collection and
 * packaging of all the bugs created on the FlyTrap view. This will be returned to the calling
 * activity to act upon based on configured settings.
 *
 * i.e. Upload to custom server, Upload to Google Drive, Prompt user to email it to developer,
 *      Upload to service hosted by me (thing how Crashlytics works).
 *
 * Created by drew.heavner on 7/7/14.
 */
public class Report {

    /************************************************
     *
     * Variables
     *
     */

    private String title;
    private long timestamp;
    private List<Bug> bugs;

    private String baseScreenShot;
    private String shadeScreenShot;

    /**
     * Empty Constructor
     */
    private Report(){
        bugs = new ArrayList<>();
        timestamp = System.currentTimeMillis();
    }

    public String getTitle(){ return title; }
    public long getTimestamp(){ return timestamp; }
    public List<Bug> getBugs(){ return bugs; }
    public String getBaseScreenshot(){ return baseScreenShot; }
    public String getShadeScreenshot(){ return shadeScreenShot; }

    /**
     * Generate the bug report into a temp zip file
     * on the disk to be used to upload to a server or
     * to storage dump
     *
     * @return
     */
    public void generateReport(final Context ctx){

        // Serialize information to JSON
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                // Generate a title
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                title = String.format("TRAP_REPORT_%s", timestamp);

                // Serialize metadata into JSON
                JSONObject meta = new JSONObject();
                FileWriter writer = null;
                try {
                    meta.put("title", title);
                    meta.put("timestamp", timestamp);

                    // Insert all the bugs
                    JSONArray bugData = new JSONArray();
                    for(Bug bug: bugs){
                        bugData.put(bug.toJSON());
                    }
                    meta.put("bugs", bugData);

                    // Create the new item directory
                    File reportDir = new File(ctx.getFilesDir(), title);
                    reportDir.mkdir();

                    // Write the meta json to the dir
                    File metaFile = new File(reportDir, "metadata.json");
                    writer = new FileWriter(metaFile);
                    writer.write(meta.toString());
                    writer.close();
                    writer = null;

                    // Now copy over the saved screenshots from
                    File baseScreen = new File(baseScreenShot);
                    File shadeScreen = new File(shadeScreenShot);

                    File baseOutput = new File(reportDir, baseScreen.getName());
                    File shadeOutput = new File(reportDir, shadeScreen.getName());

                    boolean cpResult1 = copy(baseScreen, baseOutput);
                    boolean cpResult2 = copy(shadeScreen, shadeOutput);

                    if(cpResult1 && cpResult2){
                        // All file and data are now in the report directory, now we must compress the directory into a zip file



                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    if(writer != null){
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }




                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {


            }
        }.execute();

        // Save screenshot data to disk


        // Generate ZIP file


    }

    /**
     * Copy files from one source to another
     *
     * @param input     the input source file
     * @param output    the destination file
     * @return          true if operation was successful
     *
     * @throws IOException
     */
    private static boolean copy(File input, File output) throws IOException {

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(input);
            fos = new FileOutputStream(output);

            byte[] buffer = new byte[256];

            int count;
            while ((count = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }

            fis.close();
            fis = null;

            fos.close();
            fos = null;

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }

            if(fos != null){
                fos.close();
            }
        }

        return false;
    }


    /**
     * Bug Report builder
     *
     */
    public static class Builder{

        // The tracking report instance
        Report report;

        /**
         * Create a new builder object to construct a new
         * bug report
         */
        public Builder(){
            report = new Report();
        }

        /**
         * Add a single bug to the report
         *
         * @param bug       the bug to add
         * @return
         */
        public Builder addBug(Bug bug){
            report.bugs.add(bug);
            return this;
        }

        public Builder addBugs(Collection<? extends Bug> bugs){
            report.bugs.addAll(bugs);
            return this;
        }

        public Builder setTitle(String title){
            report.title = title;
            return this;
        }

        public Builder setBaseScreenshot(String path){
            report.baseScreenShot = path;
            return this;
        }

        public Builder setShadeScreenshot(String path){
            report.shadeScreenShot = path;
            return this;
        }

        /**
         * Build and Return the report
         * @return
         */
        public Report build(){
            return report;
        }

    }

}
