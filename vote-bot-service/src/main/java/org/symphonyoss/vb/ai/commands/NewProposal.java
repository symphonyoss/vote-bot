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
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.vb.constants.Constants;
import org.symphonyoss.vb.model.Proposal;
import org.symphonyoss.vb.model.VoteProposal;
import org.symphonyoss.vb.services.JsonPersist;
import org.symphonyoss.vb.services.VoteBotProposalService;
import org.symphonyoss.vb.services.VoteProposalWorker;

import java.util.Calendar;

/**
 * Created by frank on 3/24/17.
 */
public class NewProposal extends Command {

    VoteBotProposalService voteBotProposalService;
    SymMessage symMessage;
    private final Logger logger = LoggerFactory.getLogger(NewProposal.class);

    public NewProposal(VoteBotProposalService voteBotProposalService, SymMessage symMessage) {

        this.voteBotProposalService = voteBotProposalService;
        this.symMessage = symMessage;
        setName(Constants.NEW_PROPOSAL_COMMAND);
    }

    @Override
    public boolean exec() {


        String details = symMessage.getMessageText().replace("&nbsp;", " ").trim();

        if (details.length() - details.replace("`", "").length() < 4) {

            return false;

        }


        details = details.substring(details.indexOf("`") + 1);
        String shortDesc = details.substring(0, details.indexOf("`"));
        String fullDesc = details.substring(details.indexOf("`") + 1);
        fullDesc = fullDesc.replace("`", "");


        Proposal proposal = new Proposal();
        proposal.setName(shortDesc);
        proposal.setDescription(fullDesc);

        VoteProposal voteProposal = new VoteProposal();
        voteProposal.setProposal(proposal);
        voteProposal.addParticipants(voteBotProposalService.getMembersWhoCanVote());
        voteProposal.setVoteId(voteBotProposalService.getLastVoteId() + 1);
        voteBotProposalService.setLastVoteId(voteProposal.getVoteId());

        voteProposal.setDuration(getDuration());

        voteProposal.setReminderFrequency(Constants.DEFAULT_REMINDER_TIME);

        voteBotProposalService.getActiveVotingProposals().put(voteProposal.getVoteId(), voteProposal);


        VoteProposalWorker voteProposalWorker = new VoteProposalWorker(voteProposal);


        new Thread(voteProposalWorker).start();

        try {

            JsonPersist.writeVoteSession(voteProposal);


        } catch (Exception e) {

            logger.error("Could not write out json", e);
        }

        return true;

    }


    private long getDuration() {

        Calendar calendar = Calendar.getInstance();

        //Fast forward to Monday morning
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {

            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
        }


        for (int i = 0; i < Constants.DEFAULT_DURATION_DAYS; ) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            //Calc business days.  This can be extended for holidays..etc.
            if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY ) {
                i++;
            }

        }


        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }


}
