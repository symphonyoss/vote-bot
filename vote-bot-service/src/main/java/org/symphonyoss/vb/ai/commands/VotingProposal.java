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
import org.symphonyoss.vb.model.Vote;
import org.symphonyoss.vb.model.VoteProposal;
import org.symphonyoss.vb.model.VoteProposalFormatter;
import org.symphonyoss.vb.model.VoteType;
import org.symphonyoss.vb.services.JsonPersist;
import org.symphonyoss.vb.services.VoteBotProposalService;

/**
 * Created by frank on 3/28/17.
 */
public class VotingProposal extends Command {


    VoteBotProposalService voteBotProposalService;
    SymMessage symMessage;
    private final Logger logger = LoggerFactory.getLogger(VotingProposal.class);

    public VotingProposal(VoteBotProposalService voteBotProposalService, SymMessage symMessage) {

        this.voteBotProposalService = voteBotProposalService;
        this.symMessage = symMessage;
        setName(Constants.VOTE_COMMAND);
    }

    @Override
    public boolean exec() {

        String[] chunks = symMessage.getMessageText().replace("\u00a0", " ").replace("&nbsp;", " ").split("\\s+");


        logger.info("Vote being cast {} {} {} ", symMessage.getFromUserId(), chunks.length);

        if (chunks.length >= 3 && chunks[1].matches("^-?\\d+$")) {

            logger.info("Vote being cast {} {} {} ", symMessage.getFromUserId(), chunks[1], chunks[2]);

            Integer voteId = new Integer(chunks[1].trim());


            String voteType = chunks[2].trim().toLowerCase();


            VoteType type = VoteType.convertType(voteType);


            if (type != null) {


                VoteProposal voteProposal = voteBotProposalService.getActiveVotingProposals().get(voteId);


                if (voteProposal != null) {
                    logger.info("Casting Vote {} {} {} ", symMessage.getFromUserId(), chunks[1], chunks[2]);
                    Vote vote = new Vote();
                    vote.setVote(VoteType.fromString(type.toString()));
                    vote.setSymUser(voteBotProposalService.getMemberChats().get(symMessage.getFromUserId()).getRemoteUsers().iterator().next());
                    vote.setVoteTime(System.currentTimeMillis());

                    replyVoteCast(symMessage, voteProposal.getVoteId(), voteProposal.castVote(vote));

                    try {

                        JsonPersist.writeVoteSession(voteProposal);


                    } catch (Exception e) {

                        logger.error("Could not write out json", e);
                    }

                    return true;
                }

            }


        }

        return false;
    }

    private void replyVoteCast(SymMessage symMessage, Integer voteId, boolean result) {

        if (result) {

            reply(symMessage, voteId);

        } else {
            symMessage.setMessage(Constants.FAILED_VOTE_MESSAGE);

            voteBotProposalService.replyToMessage(symMessage);

        }

    }


    private void reply(SymMessage symMessage, Integer voteId) {

        StringBuilder message = new StringBuilder();
        message.append(MLTypes.START_ML);


        message.append(Constants.VOTE_SEARCH_HEADER);


        VoteProposal voteProposal = voteBotProposalService.getActiveVotingProposals().get(voteId) != null ? voteBotProposalService.getActiveVotingProposals().get(voteId) : voteBotProposalService.getArchiveVotingProposals().get(voteId);

        if (voteProposal != null)
            message.append(VoteProposalFormatter.getStatusMessageFull(voteProposal, false, symMessage.getFromUserId()));


        message.append(MLTypes.END_ML);
        symMessage.setMessage(message.toString());

        voteBotProposalService.replyToMessage(symMessage);
    }

}
