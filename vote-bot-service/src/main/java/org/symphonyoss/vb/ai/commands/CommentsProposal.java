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

import org.symphonyoss.client.common.MLTypes;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.vb.constants.Constants;
import org.symphonyoss.vb.model.VoteComment;
import org.symphonyoss.vb.model.VoteProposal;
import org.symphonyoss.vb.model.VoteProposalFormatter;
import org.symphonyoss.vb.services.JsonPersist;
import org.symphonyoss.vb.services.VoteBotProposalService;

/**
 * Created by frank on 3/24/17.
 */
public class CommentsProposal extends Command{


    VoteBotProposalService voteBotProposalService;
    SymMessage symMessage;

    public CommentsProposal(VoteBotProposalService voteBotProposalService, SymMessage symMessage) {

        this.voteBotProposalService = voteBotProposalService;
        this.symMessage = symMessage;
        setName(Constants.COMMENT_VOTE_COMMAND);
    }

    @Override
    public boolean exec() {

        try {


            String[] chunks = symMessage.getMessageText().replace("\u00a0", " ").replace("&nbsp;", " ").split("\\s+");


            SymUser symUser = voteBotProposalService.getMembers().stream().filter(symUser1 -> symUser1.getId().equals(symMessage.getFromUserId())).findFirst().orElse(null);

            if (chunks.length >= 3 && symUser != null) {

                if (chunks[1].matches("^-?\\d+$")) {



                    String comment = symMessage.getMessage().substring(symMessage.getMessage().indexOf(">"), symMessage.getMessage().lastIndexOf("<"));
                    comment = comment.substring(comment.indexOf(chunks[1]) + chunks[1].length());



                    //Comments on active votes only.
                    VoteProposal voteProposal = voteBotProposalService.getActiveVotingProposals().get(new Integer(chunks[1]));

                    if (voteProposal != null) {

                        VoteComment voteComment = new VoteComment();
                        voteComment.setId(voteProposal.getComments().size() + 1);
                        voteComment.setTimestamp(System.currentTimeMillis());
                        voteComment.setSymUser(symUser);



                        voteComment.setComment(comment);

                        voteProposal.getComments().add(voteComment);

                        JsonPersist.writeVoteSession(voteProposal);
                        StringBuilder stringBuilder = new StringBuilder();

                        stringBuilder.append(MLTypes.START_ML);
                        stringBuilder.append(Constants.MSG_PROPOSAL_HEADER_COMMENT);
                        stringBuilder.append(Constants.SEPARATOR_ML);
                        stringBuilder.append("VoteID: ").append(voteProposal.getVoteId()).append("  ").append(voteProposal.getProposal().getName()).append(MLTypes.BREAK);
                        stringBuilder.append(VoteProposalFormatter.getCommentsMessage(voteProposal,voteProposal.getComments().size())).append(MLTypes.BREAK);
                        stringBuilder.append(MLTypes.END_ML);


                        symMessage.setMessage(stringBuilder.toString());
                        voteBotProposalService.sendMessage(symMessage);
                        voteBotProposalService.relayMessage(symMessage);

                        return true;
                    }

                }


            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        return false;
    }


}
