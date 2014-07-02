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

/**
 * This is the object representation of the 'attachments' in the
 * Bug class to provide extra information from the user about a bug to
 * the developer. This is the class that holds that information.
 *
 * Created by drew.heavner on 7/2/14.
 */
public class Comment {

    /***************************************************************************
     *
     * Variables
     *
     */

    /**
     * The text content of the comment that the user attaches to a bug
     */
    private String text;

    /**
     * Constructor
     */
    public Comment(String text){
        this.text = text;
    }

    /**
     * Override the to string method to return the text content of the comment
     *
     * @return      the comment text
     */
    @Override
    public String toString(){
        return text;
    }

}
