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
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;
import android.util.Pair;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * This delivery will send a POST request to the URL endpoint supplied in the constructor
 * in the format of JSON body
 *
 * Multipart FORM upload
 *
 * 'meta' -
 *  {
 *      title: "TRAP_REPORT_YYYYMMDD_HHMMSS",
 *      timestamp: 0000000000,
 *      bugs: [
 *          {
 *              id: 000000000,
 *              center: {
 *                  x: 0,
 *                  y: 0
 *              },
 *              radius: 0.0,
 *              accent_color: 0,
 *              comment: "This switch is broken"
 *          },
 *          ...
 *      ]
 *
 *  }
 *
 *  'base' - is the base screenshot of the application
 *  'shade' - is the flytrap feedback screen that lays on top of the base
 *
 *  Created by drew.heavner on 7/9/14.
 */
public class APIDelivery extends Delivery {

    /************************************************
     *
     * Constants
     *
     */

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType PNG = MediaType.parse("image/png");

    /************************************************
     *
     * Variables
     *
     */

    /**
     * The API Endpoint URL to send the feedback data to
     */
    public String url;

    /**
     * Constructor
     *
     * @param url   the api endpoint url
     */
    public APIDelivery(String url){
        this.url = url;
    }

    /**
     * Parcel Constructor
     *
     * @param in    the input data parcel
     */
    public APIDelivery(Parcel in){
        url = in.readString();
    }

    /************************************************
     *
     * Delivery Methods
     *
     */

    @Override
    public void onReportGenerated(final Context ctx, final Report report, final OnReportHandler handler) {
        // Generate API request and data in an async task
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... params) {

                report.generateAPIReport(ctx, new Report.OnAPIReportGeneratedListener() {
                    @Override
                    public void onGenerated(JSONObject meta, Pair<File, File> screens) {

                        // Build the multipart body
                        OkHttpClient client = new OkHttpClient();
                        RequestBody body = new MultipartBuilder()
                                .type(MultipartBuilder.FORM)
                                .addPart(
                                    Headers.of("Content-Disposition", "form-data; name=meta"),
                                    RequestBody.create(JSON, meta.toString())
                                )
                                .addPart(
                                    Headers.of("Content-Disposition", "form-data; name=base"),
                                    RequestBody.create(PNG, screens.first)
                                )
                                .addPart(
                                    Headers.of("Content-Disposition", "form-data; name=shade"),
                                    RequestBody.create(PNG, screens.second)
                                )
                                .build();

                        // Build the request
                        Request request = new Request.Builder()
                                .url(url)
                                .post(body)
                                .build();

                        // Execute request
                        try {
                            client.newCall(request).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Post finish in the main thread
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                handler.onFinish();
                            }
                        });

                    }

                    @Override
                    public void onFailure() {
                        Log.e(APIDelivery.class.getName(), "Failed to generate api report to send to server");
                        handler.onFinish();
                    }
                });

                return null;
            }
        }.execute();


    }

    @Override
    public int getType() {
        return Delivery.TYPE_API;
    }

    /************************************************
     *
     * Parcelable Methods
     *
     */

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
    }

    /**
     * The parcel creator for this class
     */
    public static final Creator<APIDelivery> CREATOR = new Creator<APIDelivery>() {
        @Override
        public APIDelivery createFromParcel(Parcel source) {
            return new APIDelivery(source);
        }

        @Override
        public APIDelivery[] newArray(int size) {
            return new APIDelivery[size];
        }
    };
}
