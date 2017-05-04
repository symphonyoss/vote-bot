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

package org.symphonyoss.vb.constants;

import org.symphonyoss.client.common.MLTypes;

/**
 * Created by frank on 12/14/16.
 */
public class Constants {
    public static final String ACTIVE_VOTE_LIST_HEADER = ".<br/><b>***Active Vote List***</b><br/>";
    public static final String VOTE_SEARCH_HEADER = ".<br/><b>***Vote Search Result***</b><br/>";
    public static final String MSG_NOTIFICATION_HEADER = ".<br/><b>***VOTE NOTIFICATION***</b><br/>";
    public static final String MSG_PROPOSAL_HEADER = "Proposal Vote<br/>";
    public static final String MSG_PROPOSAL_HEADER_REMINDER = "Proposal Vote Reminder<br/>";
    public static final String MSG_PROPOSAL_HEADER_COMMENT = ".<br/><b>NEW COMMENT</b><br/>";


    public static final String MSG_TIME_REMAINING = "End Time: ";
    public static final String MSG_DETAILS = "<b>Detail:</b> ";
    public static final String MSG_STATUS_BREAK = ".<br/><b>****VOTE RESULTS****</b><br/>";

    public static final String YEA = "yea";
    public static final String NAY = "nay";


    public static final String NEW_PROPOSAL_COMMAND = "/new";
    public static final String CANCEL_VOTE_COMMAND = "[/cancel";
    public static final String LIST_VOTE_COMMAND = "/list";
    public static final String USAGE_COMMAND = "/usage";
    public static final String HELP_COMMAND = "/help";
    public static final String VOTE_COMMAND = "/vote";
    public static final String COMMENT_VOTE_COMMAND = "/comment";


    public static final String ABSTAIN = "abstain";
    public static final long DEFAULT_DURATION_TIME = 259200000;
    public static final int DEFAULT_DURATION_DAYS = 3;
    public static final long DEFAULT_REMINDER_TIME = 28800000;
    public static final String ARCHIVE_PARAM = "archive";
    public static final String VOTEID_PARAM = "voteid";
    public static final String ACTIVE_PARAM = "active";




    public static final String SEPARATOR_ML = "-----------------------------------------<br/>";
    public static final String NEW_LINE = "\n";


    public static final String FAILED_VOTE_MESSAGE =  "<messageML>Failed to cast vote.... Either syntax issue or you have already voted.  Try /list (voteID) to find out.</messageML>";


    public static final String USAGE =
            MLTypes.START_ML.toString() + MLTypes.START_BOLD.toString() + "Usage: " + MLTypes.END_BOLD.toString() + MLTypes.BREAK.toString() +
                    NEW_PROPOSAL_COMMAND + " [`Short Desc`] [`Detailed Desc`]     Create a new vote." + MLTypes.BREAK.toString() +
                    VOTE_COMMAND + " [VoteID] [yea/nay/abstain]                   Place a vote." + MLTypes.BREAK.toString() +
                    LIST_VOTE_COMMAND + " optional:[active/VoteID/archive p#]     List votes by criteria (default: active)" + MLTypes.BREAK.toString() +
                    COMMENT_VOTE_COMMAND + " [VoteID] [comment]                       Add a comment to a registered vote" + MLTypes.BREAK.toString() +
                    CANCEL_VOTE_COMMAND + " [VoteID]                                                 Cancel an active vote." + MLTypes.BREAK.toString() +
                    MLTypes.END_ML.toString();



    public static final String CHECK_EMOJI_ML = ":white<i>check</i>mark:";
    public static final String THUMBSUP_EMOJI_ML = ":+1:";
    public static final String THUMBSDOWN_EMOJI_ML = ":thumbsdown:";
    public static final String ABSTAIN_EMOJI_ML = ":worried:";
    public static final String NOVOTE_EMOJI_ML = "<b>You haven't voted yet!</b>";


    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mma z";


    public static final String NOVOTE_TEXT = "You haven't voted yet!";
    public static final String MAIL_READER_POLL_SLEEP = "mail.reader.poll.sleep";
    public static final String EMAIL_FLAG = "email";
}