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


import org.symphonyoss.vb.constants.Constants;

/**
 * Created by frank on 12/12/16.
 */
public enum VoteType {

    YEA("yea"),
    NAY("nay"),
    ABSTAIN("abstain");


    private final String text;

    VoteType(String text) {
        this.text = text;
    }

    public String toString() {
        return this.text;
    }

    public static VoteType fromString(String text) {
        for (VoteType b : VoteType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }


    public static VoteType convertType(String type) {

        type = type.toLowerCase();


        if (type.matches("y|yes|yea|yep|:\\+1:|\\+1|\\uD83D\\uDC4D|\\uD83D\\uDC4D\\uD83C\\uDFFB|\\uD83D\\uDC4D\\uD83C\\uDFFC|\\uD83D\\uDC4D\\uD83C\\uDFFD|\\uD83D\\uDC4D\\uD83C\\uDFFE|\\uD83D\\uDC4D\\uD83C\\uDFFF|\\u2705|\\u2714\\uFE0F|\\u2611\\uFE0F")) {
            return YEA;

        } else if (type.matches("n|no|nay|nope|:thumbsdown:|\\-1|\\uD83D\\uDC4E|\\uD83D\\uDC4E\\uD83C\\uDFFB|\\uD83D\\uDC4E\\uD83C\\uDFFC|\\uD83D\\uDC4E\\uD83C\\uDFFD|\\uD83D\\uDC4E\\uD83C\\uDFFE|\\uD83D\\uDC4E\\uD83C\\uDFFF|\\u274E|\\u274C|\\u2716\\uFE0F")) {
            return NAY;

        } else if (type.matches("a|abstain|abs|:worried:|0|\\uD83D\\uDCA9")) {
            return ABSTAIN;


        }


        return null;


    }


    public static String getEmojiMl(VoteType voteType) {

        if (voteType.equals(NAY))
            return Constants.THUMBSDOWN_EMOJI_ML;
        if (voteType.equals(YEA))
            return Constants.THUMBSUP_EMOJI_ML;
        if (voteType.equals(ABSTAIN))
            return Constants.ABSTAIN_EMOJI_ML;

        return Constants.NOVOTE_EMOJI_ML;

    }

}
