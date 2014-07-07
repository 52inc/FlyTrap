package com.ftinc.flytrap.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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


    /**
     * Generate the bug report into a temp zip file
     * on the disk to be used to upload to a server or
     * to storage dump
     *
     * @return
     */
    public File generateReport(){

        // Serialize information to JSON


        // Save screenshot data to disk


        // Generate ZIP file


        return null;
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

        /**
         * Build and Return the report
         * @return
         */
        public Report build(){
            return report;
        }

    }

}
