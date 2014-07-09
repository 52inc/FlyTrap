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
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.ftinc.flytrap.util.Utils;

import java.io.File;

/**
 * Created by drew.heavner on 7/9/14.
 */
public class EmailDelivery extends Delivery {

    /************************************************
     *
     * Variables
     *
     */

    /**
     * The target email address of the developer to send
     * feedback to
     */
    public String address;

    /**
     * The email subject to compose
     */
    public String subject;

    /**
     * The email message to compose
     */
    public String message;

    /**
     * Constructor
     *
     * @param addr          the target email address
     * @param subject       the subject of the email to format
     * @param message       the message of the email to format
     */
    public EmailDelivery(String addr, String subject, String message){
        this.address = addr;
        this.subject = subject;
        this.message = message;
    }

    /**
     * Parcelable Constructor
     *
     * @param in    the constructing parcel
     */
    public EmailDelivery(Parcel in){
        this.address = in.readString();
        this.subject = in.readString();
        this.message = in.readString();
    }

    /************************************************
     *
     * Helper Methods
     *
     */

    @Override
    public void onReportGenerated(final Context ctx, Report report, final OnReportHandler handler) {
        // Generate the compressed zip of the report then prepare intent to be sent
        report.generateCompressedReport(ctx, new Report.OnCompressedReportGeneratedListener() {
            @Override
            public void onGenerated(File zipFile) {

                // File generated! Prepare the intent to send this file off via email
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, message);

                // Attach the report file
                Uri uri = Uri.parse("file://" + zipFile.getPath());
                intent.putExtra(Intent.EXTRA_STREAM, uri);

                // Check for intent availability
                if (Utils.isIntentAvailable(ctx, intent)) {
                    // Send intent
                    ctx.startActivity(intent);
                } else {
                    Log.w(EmailDelivery.class.getName(), "Unable to send report, no email client available.");
                }

                handler.onFinish();
            }

            @Override
            public void onFailure() {
                Log.w(EmailDelivery.class.getName(), "Failed to generate zip file of the report!");
                handler.onFinish();
            }
        });

    }

    @Override
    public int getType() {
        return Delivery.TYPE_EMAIL;
    }

    /************************************************
     *
     * Parcelable Methods
     *
     */

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(subject);
        dest.writeString(message);
    }

    /**
     * Parcelable CREATOR for this class
     *
     */
    public static final Creator<EmailDelivery> CREATOR = new Creator<EmailDelivery>() {
        @Override
        public EmailDelivery createFromParcel(Parcel source) {
            return new EmailDelivery(source);
        }

        @Override
        public EmailDelivery[] newArray(int size) {
            return new EmailDelivery[size];
        }
    };
}
