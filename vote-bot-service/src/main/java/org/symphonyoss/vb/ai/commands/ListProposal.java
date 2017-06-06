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

package org.symphonyoss.vb.ai.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.common.MLTypes;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.vb.constants.Constants;
import org.symphonyoss.vb.model.VoteProposal;
import org.symphonyoss.vb.model.VoteProposalFormatter;
import org.symphonyoss.vb.services.VoteBotProposalService;

import java.util.TreeMap;

/**
 * Created by frank on 3/28/17.
 */
public class ListProposal extends Command{

    VoteBotProposalService voteBotProposalService;
    SymMessage symMessage;
    private final Logger logger = LoggerFactory.getLogger(ListProposal.class);

    public ListProposal(VoteBotProposalService voteBotProposalService, SymMessage symMessage) {

        this.voteBotProposalService = voteBotProposalService;
        this.symMessage = symMessage;
        setName(Constants.LIST_VOTE_COMMAND);
    }


    @Override
    public boolean exec(){

        String[] chunks = symMessage.getMessageText().replace("\u00a0", " ").replace("&nbsp;", " ").split("\\s+");

        if (chunks.length == 2) {
            replyList(symMessage, chunks[1].toLowerCase(), null);
        } else if (chunks.length == 3) {
            replyList(symMessage, chunks[1].toLowerCase(), chunks[2].toLowerCase());
        } else {
            replyList(symMessage, null, null);
        }

        return true;
    }


    private void replyList(SymMessage symMessage, String type, String arg) {

        StringBuilder message = new StringBuilder();
        message.append(MLTypes.START_ML);

        logger.info("List command issued with args {} {}", type, arg);

        if (type == null || type.equals(Constants.ACTIVE_PARAM)) {

            message.append(Constants.ACTIVE_VOTE_LIST_HEADER);


            for (VoteProposal voteProposal : voteBotProposalService.getActiveVotingProposals().values()) {

                message.append(MLTypes.BREAK).append(VoteProposalFormatter.getStatusMessageCompact(voteProposal, symMessage.getFromUserId()));
            }


        } else if (type.matches("^-?\\d+$")) {


            Integer voteId = new Integer(type);

             message.append(Constants.VOTE_SEARCH_HEADER);


            VoteProposal voteProposal = voteBotProposalService.getActiveVotingProposals().get(voteId) != null ? voteBotProposalService.getActiveVotingProposals().get(voteId) : voteBotProposalService.getArchiveVotingProposals().get(voteId);

            if (voteProposal != null)
                message.append(VoteProposalFormatter.getStatusMessageFull(voteProposal, false, symMessage.getFromUserId()));


        } else if (type.equals(Constants.ARCHIVE_PARAM)) {
            message.append(Constants.VOTE_SEARCH_HEADER);

            TreeMap<Integer, VoteProposal> orderedMap = new TreeMap<>(voteBotProposalService.getArchiveVotingProposals());

            Integer page = 1;

            if (arg != null && arg.matches("^-?\\d+$"))
                page = Integer.valueOf(arg);


            int pages = (int) Math.ceil(orderedMap.values().size() / 5) + 1;

            if (page > pages)
                page = 1;

            int startPos = (page * 5) - 5;

            int row = 0;
            for (VoteProposal voteProposal : orderedMap.values()) {

                if (row >= startPos && (startPos + 4) >= row) {
                    message.append(MLTypes.BREAK).append(VoteProposalFormatter.getStatusMessageCompact(voteProposal, symMessage.getFromUserId()));


                }

                ++row;
            }

            message.append(MLTypes.BREAK).append(Constants.SEPARATOR_ML);
            message.append(MLTypes.BREAK).append("Page: ").append(page).append(" of ").append(pages).append(MLTypes.BREAK);


        }


        message.append(MLTypes.END_ML);
        symMessage.setMessage(message.toString());

        voteBotProposalService.replyToMessage(symMessage);

    }
}
