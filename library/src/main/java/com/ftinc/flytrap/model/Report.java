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

package com.ftinc.flytrap.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.ftinc.flytrap.util.Utils;

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

    /************************************************
     *
     * Accessor Methods
     *
     */

    public String getTitle(){ return title; }
    public long getTimestamp(){ return timestamp; }
    public List<Bug> getBugs(){ return bugs; }
    public String getBaseScreenshot(){ return baseScreenShot; }
    public String getShadeScreenshot(){ return shadeScreenShot; }

    /************************************************
     *
     * Helper Methods
     *
     */

    /**
     * Generate the data needed to send this report to an
     * API webserver service.
     *
     * @param ctx       the application context
     */
    public void generateAPIReport(final Context ctx, final OnAPIReportGeneratedListener listener){

        // Generate a title
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        title = String.format("TRAP_REPORT_%s", timestamp);

        // Serialize metadata into JSON
        JSONObject meta = new JSONObject();
        try {
            meta.put("title", title);
            meta.put("timestamp", timestamp);

            // Insert all the bugs
            JSONArray bugData = new JSONArray();
            for (Bug bug : bugs) {
                bugData.put(bug.toJSON());
            }
            meta.put("bugs", bugData);

            // Create the new item directory
            File reportDir = new File(ctx.getExternalCacheDir(), title);
            reportDir.mkdir();

            // Now copy over the saved screenshots from
            File baseScreen = new File(baseScreenShot);
            File shadeScreen = new File(shadeScreenShot);

            File baseOutput = new File(reportDir, baseScreen.getName());
            File shadeOutput = new File(reportDir, shadeScreen.getName());

            boolean cpResult1 = copy(baseScreen, baseOutput);
            boolean cpResult2 = copy(shadeScreen, shadeOutput);

            if (cpResult1 && cpResult2) {
                // Return the result
                listener.onGenerated(meta, Pair.create(baseOutput, shadeOutput));
            }

        } catch(JSONException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listener.onFailure();
    }

    /**
     * Generate the bug report into a temp zip file
     * on the disk to be used to upload to a server or
     * to storage dump
     *
     * @param ctx       the application context
     * @param listener  the listener callback
     */
    public void generateCompressedReport(final Context ctx, final OnCompressedReportGeneratedListener listener){

        /*
         * Generate the report into a compressed archive to be sent to the developer
         */
        new AsyncTask<Void, Void, File>(){
            @Override
            protected File doInBackground(Void... params) {

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
                    File reportDir = new File(ctx.getExternalCacheDir(), title);
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
                        File output = new File(ctx.getExternalCacheDir(), title.concat(".zip"));
                        if(Utils.compress(reportDir, output)){
                            return output;
                        }
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
            protected void onPostExecute(File zipFile) {
                if(zipFile != null){
                    listener.onGenerated(zipFile);
                }else{
                    listener.onFailure();
                }
            }
        }.execute();
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


    /************************************************
     *
     * Interfaces and Classes
     *
     */


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

    /**
     * An interface callback for generating a report asynchronously
     * in the background.
     */
    public static interface OnCompressedReportGeneratedListener {
        public void onGenerated(File zipFile);
        public void onFailure();
    }

    /**
     * An interface callback for generating the data needed from the report
     * to send the report to a webservice API to be collected by the developer
     *
     */
    public static interface OnAPIReportGeneratedListener{
        public void onGenerated(JSONObject meta, Pair<File, File> screens);
        public void onFailure();
    }

}
