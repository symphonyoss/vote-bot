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


import org.apache.commons.lang.StringUtils;
import org.symphonyoss.client.common.MLTypes;
import org.symphonyoss.vb.constants.Constants;
import org.symphonyoss.vb.util.TimeDateFormatting;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.symphonyoss.client.common.MLTypes.*;
import static org.symphonyoss.vb.constants.Constants.*;


/**
 * Created by frank on 3/26/17.
 */
public class VoteProposalFormatter {

    private static final String SEP = "---------------------------------------------------------------------------------";

    public VoteProposalFormatter() {


    }


    public static String getStatusMessage(VoteProposal voteProposal, boolean newVote, Long userId) {

        StringBuilder message = new StringBuilder();
        message.append(MLTypes.START_ML)
                .append(Constants.MSG_NOTIFICATION_HEADER)
                .append(getStatusMessageFull(voteProposal, newVote, userId))
                .append(MLTypes.END_ML);

        return message.toString();
    }


    public static String getStatusMessageFull(VoteProposal voteProposal, boolean newVote, Long userId) {

        StringBuilder message = new StringBuilder();

        message.append(getVoteHeader(voteProposal, userId));
        message.append(START_BOLD + "Detail:   " + END_BOLD).append(voteProposal.getProposal().getDescription()).append(BREAK);

        message.append(SEPARATOR_ML);
        message.append("# Participants: ").append(voteProposal.getParticipants().size()).append(BREAK);
        message.append(THUMBSUP_EMOJI_ML + ": ").append((int) voteProposal.getYeas()).append(" | ").append(getYeasMessage(voteProposal.getVotes(), false)).append(BREAK);
        message.append(THUMBSDOWN_EMOJI_ML + ": ").append((int) voteProposal.getNays()).append(" | ").append(getNaysMessage(voteProposal.getVotes(), false)).append(BREAK);
        message.append(ABSTAIN_EMOJI_ML + ": ").append((int) voteProposal.getAbstains()).append(" | ").append(getAbstainsMessage(voteProposal.getVotes(), false)).append(BREAK);
        message.append("Outstanding: " + " | ").append(getOutstandingMessage(voteProposal, false)).append(BREAK);
        message.append("Result: ").append(voteProposal.getResult().toString());
        message.append(BREAK).append(getCommentsMessage(voteProposal, -1));

        if (newVote)
            message.append(BREAK + "<hash tag=\"NEWVOTE\"/>");

        return message.toString();

    }


    public static String getMailStatusMessage(VoteProposal voteProposal, boolean newVote, Long userId) {

        StringBuilder message = new StringBuilder();

        String votedEmojiMl = Constants.NOVOTE_TEXT;
        message.append("Vote Bot Notification").append(NEW_LINE).append(SEP).append(NEW_LINE).append(NEW_LINE);

        message.append("VoteID: ").append(voteProposal.getVoteId());
        message.append("    Status: ").append(voteProposal.getResult().toString());

        if (userId != null && voteProposal.getVotes().stream().anyMatch(vote -> vote.getSymUser().getId().equals(userId))) {

            Vote aVote = voteProposal.getVotes().stream().filter(vote -> vote.getSymUser().getId().equals(userId)).findFirst().orElse(null);

            if (aVote != null)
                votedEmojiMl = VoteType.getEmojiMl(aVote.getVote());

            message.append("    You voted: ").append(votedEmojiMl);

        }

        message.append(NEW_LINE);
        message.append("Vote Ends:   ").append(TimeDateFormatting.formatDate(voteProposal.getStartTime() + voteProposal.getDuration())).append(NEW_LINE).append(NEW_LINE);
        message.append("Proposal: ").append(voteProposal.getProposal().getName()).append(NEW_LINE);
        message.append("Description: ").append(voteProposal.getProposal().getDescription()).append(NEW_LINE).append(NEW_LINE);


        message.append(SEP).append(NEW_LINE);
        message.append("# Participants: ").append(voteProposal.getParticipants().size()).append(NEW_LINE);
        message.append("YEA: ").append((int) voteProposal.getYeas()).append(" | ").append(getYeasMessage(voteProposal.getVotes(), true)).append(NEW_LINE);
        message.append("NAY: ").append((int) voteProposal.getNays()).append(" | ").append(getNaysMessage(voteProposal.getVotes(), true)).append(NEW_LINE);
        message.append("ABSTAIN: ").append((int) voteProposal.getAbstains()).append(" | ").append(getAbstainsMessage(voteProposal.getVotes(), true)).append(NEW_LINE);
        message.append("Outstanding: ").append(" | ").append(getOutstandingMessage(voteProposal, true)).append(NEW_LINE);
        message.append("Result: ").append(voteProposal.getResult().toString()).append(NEW_LINE).append(NEW_LINE);
        message.append(getMailCommentsMessage(voteProposal, -1)).append(NEW_LINE).append(NEW_LINE).append(NEW_LINE);

        message.append("To vote, send a message to ESCO BOT ->/vote [VoteID] [yea/nay/abstain]").append(NEW_LINE).append(NEW_LINE);
        message.append("Alternatively you can respond to the email with the same command..").append(NEW_LINE).append(NEW_LINE);


        return message.toString();

    }

    public static String getStatusMessageCompact(VoteProposal voteProposal, Long userId) {

        StringBuilder message = new StringBuilder();

        message.append(getVoteHeader(voteProposal, userId))
                .append(START_BOLD + "Detail:   " + END_BOLD)
                .append(StringUtils.abbreviate(voteProposal.getProposal().getDescription(), 80));

        return message.toString();
    }


    private static String getVoteHeader(VoteProposal voteProposal, Long userId) {

        StringBuilder header = new StringBuilder();
        String votedEmojiMl = Constants.NOVOTE_EMOJI_ML;

        header.append(SEPARATOR_ML);
        header.append(START_BOLD + "VoteID: " + END_BOLD).append(voteProposal.getVoteId());
        header.append(START_BOLD + "    Status: " + END_BOLD).append(voteProposal.getResult().toString()).append(BREAK);

        if (userId != null && voteProposal.getVotes().stream().anyMatch(vote -> vote.getSymUser().getId().equals(userId))) {

            Vote aVote = voteProposal.getVotes().stream().filter(vote -> vote.getSymUser().getId().equals(userId)).findFirst().orElse(null);

            if (aVote != null)
                votedEmojiMl = VoteType.getEmojiMl(aVote.getVote());
        }

        header.append(THUMBSUP_EMOJI_ML + ": ").append((int) voteProposal.getYeas()).append(" | ");
        header.append(THUMBSDOWN_EMOJI_ML + ": ").append((int) voteProposal.getNays()).append(" | ");
        header.append(ABSTAIN_EMOJI_ML + ": ").append((int) voteProposal.getAbstains()).append(" | ");
        header.append(START_BOLD + "V: " + END_BOLD).append(votedEmojiMl).append(BREAK);

        header.append(START_BOLD + "Ends:      " + END_BOLD).append(TimeDateFormatting.formatDate(voteProposal.getStartTime() + voteProposal.getDuration())).append(BREAK);
        header.append(START_BOLD + "Subject: " + END_BOLD).append(voteProposal.getProposal().getName()).append(BREAK);


        return header.toString();
    }


    /**
     * Get Comments by individual ID or list
     *
     * @param id Individual ID or -1 for list
     * @return MessageML (Body only) list of comments
     */
    public static String getCommentsMessage(VoteProposal voteProposal, int id) {

        StringBuilder comment = new StringBuilder();

        List<VoteComment> sortedComments = voteProposal.getComments().stream().sorted(Comparator.comparing(e -> (e.getTimestamp()))).collect(Collectors.toList());

        for (VoteComment voteComment : sortedComments) {

            if (voteComment.getId() == id || id == -1) {
                comment.append(BREAK).append(SEPARATOR_ML);
                comment.append(TimeDateFormatting.formatDate(voteComment.getTimestamp()));
                if (voteComment.getSymUser().getId() < 0) {
                    comment.append(": ").append(voteComment.getSymUser().getDisplayName()).append(" | ");
                } else {
                    //comment.append(": <mention uid=\"").append(voteComment.getSymUser().getId()).append("\"/> | ");
                    comment.append(": ").append(voteComment.getSymUser().getDisplayName()).append(" | ");
                }
                comment.append(voteComment.getComment());

            }

        }

        return comment.toString();

    }

    /**
     * Get Comments by individual ID or list
     *
     * @param id Individual ID or -1 for list
     * @return MessageML (Body only) list of comments
     */
    public static String getMailCommentsMessage(VoteProposal voteProposal, int id) {

        StringBuilder comment = new StringBuilder();

        for (VoteComment voteComment : voteProposal.getComments()) {

            if (voteComment.getId() == id || id == -1) {
                comment.append("COMMENTS:").append(NEW_LINE).append(SEP).append(NEW_LINE);
                comment.append(TimeDateFormatting.formatDate(voteComment.getTimestamp())).append(": ");
                comment.append(voteComment.getSymUser().getDisplayName()).append(": ").append(voteComment.getComment());


            }

        }

        return comment.toString();

    }


    public static String getVoteList(Set<Vote> votes, boolean isMail) {

        StringBuilder stringBuffer = new StringBuilder();
        for (Vote vote : votes) {

            if (!isMail && vote.getSymUser().getId() >= 0) {
              //  stringBuffer.append("<mention uid=\"").append(vote.getSymUser().getId()).append("\"/> | ");
                stringBuffer.append(vote.getSymUser().getDisplayName()).append(" | ");
            } else {
                stringBuffer.append(vote.getSymUser().getDisplayName()).append(" | ");
            }
        }

        return stringBuffer.toString();
    }


    private static String getOutstandingMessage(VoteProposal voteProposal, boolean isMail) {

        Set<Participant> p = new HashSet<>(voteProposal.getParticipants());


        for (Vote vote : voteProposal.getVotes()) {

            Participant ptmp = p.stream().filter(participant -> participant.getSymUser().equals(vote.getSymUser())).findFirst().orElse(null);

            if (ptmp != null)
                p.remove(ptmp);


        }

        StringBuilder stringBuffer = new StringBuilder();


        for (Participant participant : p) {

            //This needs to be fixed.
            if (!isMail && participant.getSymUser().getId() >= 0) {

                //stringBuffer.append("<mention uid=\"").append(participant.getSymUser().getId()).append("\"/> | ");
                stringBuffer.append(participant.getSymUser().getDisplayName()).append(" | ");
            } else {
                stringBuffer.append(participant.getSymUser().getDisplayName()).append(" | ");
            }
        }


        return stringBuffer.toString();


    }


    private static String getNaysMessage(Set<Vote> votes, boolean isMail) {

        return getVoteList(votes.stream().filter(vote -> vote.getVote() == VoteType.NAY).collect(Collectors.toSet()), isMail);

    }

    private static String getYeasMessage(Set<Vote> votes, boolean isMail) {
        return getVoteList(votes.stream().filter(vote -> vote.getVote() == VoteType.YEA).collect(Collectors.toSet()), isMail);

    }

    private static String getAbstainsMessage(Set<Vote> votes, boolean isMail) {

        return getVoteList(votes.stream().filter(vote -> vote.getVote() == VoteType.ABSTAIN).collect(Collectors.toSet()), isMail);

    }

    private String voteList(Set<Vote> votes, boolean isMail) {

        StringBuilder stringBuffer = new StringBuilder();
        for (Vote vote : votes) {

            if (!isMail && vote.getSymUser().getId() >= 0) {
               // stringBuffer.append("<mention uid=\"").append(vote.getSymUser().getId()).append("\"/> | ");
                stringBuffer.append(vote.getSymUser().getDisplayName()).append(" | ");
            } else {
                stringBuffer.append(vote.getSymUser().getDisplayName()).append(" | ");
            }
        }

        return stringBuffer.toString();
    }


}
