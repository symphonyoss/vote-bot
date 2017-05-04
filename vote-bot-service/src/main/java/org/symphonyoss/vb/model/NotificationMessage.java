/*
 *  Copyright 2017 The Symphony Software Foundation
 *
 *  Licensed to The Symphony Software Foundation (SSF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.symphonyoss.vb.model;

import com.google.gson.annotations.Expose;

/**
 * Created by frank.tarsillo on 8/29/2016.
 */
public class NotificationMessage {

    //TEXT or MESSAGEML
    @Expose private String type;

    @Expose private String from;

    //Comma delimited list of usernames
    @Expose private String toUsers;

    //Comma delimited list of streams
    @Expose private String toStreams;
    @Expose private String body;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getToUsers() {
        return toUsers;
    }

    public void setToUsers(String toUsers) {
        this.toUsers = toUsers;
    }

    public String getToStreams() {
        return toStreams;
    }

    public void setToStreams(String toStreams) {
        this.toStreams = toStreams;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
