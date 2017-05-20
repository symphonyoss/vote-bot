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

package org.symphonyoss.vb.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.vb.config.BotConfig;
import org.symphonyoss.vb.mail.MailerWorker;
import org.symphonyoss.vb.model.Participant;
import org.symphonyoss.vb.model.VoteProposal;
import org.symphonyoss.vb.model.VoteProposalFormatter;

import java.util.concurrent.TimeUnit;

/**
 * Created by frank on 12/12/16.
 */
public class VoteProposalWorker implements Runnable {


    private VoteProposal voteProposal;
    private final Logger logger = LoggerFactory.getLogger(VoteProposalWorker.class);
    private Participant escoPrivate = new Participant();
    private boolean recovered = false;


    public VoteProposalWorker(VoteProposal voteProposal) {

        this.voteProposal = voteProposal;

        SymUser symUser = new SymUser();
        symUser.setEmailAddress(System.getProperty(BotConfig.MAIL_DISTRO_EMAIL, "esco-test@symphony.foundation"));
        escoPrivate.setSymUser(symUser);


    }


    @Override
    public void run() {

        if (voteProposal.getStartTime() == 0)
            voteProposal.setStartTime(System.currentTimeMillis());


        //Note this crazy stuff is meant to deal with restarts.
        long initRemindTime = (System.currentTimeMillis() - voteProposal.getStartTime()) % voteProposal.getReminderFrequency();

        if (initRemindTime == 0)
            initRemindTime = voteProposal.getReminderFrequency() - (System.currentTimeMillis() - voteProposal.getStartTime());

        long remindTime = System.currentTimeMillis() + initRemindTime;

        logger.info("Init Remind in [{}]]", initRemindTime);



        remind(true);

        while (voteProposal.getStartTime() + voteProposal.getDuration() >= System.currentTimeMillis()) {


            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {

                logger.error("", e);
            }


            if (System.currentTimeMillis() >= remindTime) {

                remindTime += voteProposal.getReminderFrequency();
                remind(false);

            }


        }

        voteProposal.setCompleted(true);
        logger.info("Vote has ended for proposal {} with result {}", voteProposal.getDescription(), voteProposal.getResult());


        voteProposal.update();
        voteProposal.setCompleted(true);

        try {

            JsonPersist.writeVoteSession(voteProposal);

        } catch (Exception e) {

            logger.error("Could not write out json", e);
        }

        logger.info("Vote session complete for proposal [{}] {} {} ", voteProposal.getVoteId(), voteProposal.getProposal().getName(), voteProposal.getProposal().getDescription());

        VoteBotProposalService.get().archiveVotingProposal(voteProposal.getVoteId());


        sendResults();


    }


    private SymMessage formatMessage(Participant participant, boolean newVote) {

        SymMessage symMessage = new SymMessage();


        symMessage.setFormat(SymMessage.Format.MESSAGEML);


        symMessage.setSymUser(participant.getSymUser());

        symMessage.setMessage(VoteProposalFormatter.getStatusMessage(voteProposal, newVote, participant.getSymUser().getId()));


        return symMessage;

    }

    private void remind(boolean newVote) {


        for (Participant participant : voteProposal.getParticipants()) {

            if (!participant.isVoteCast()) {

                logger.info("REMIND: {}: {}", participant.getSymUser().getDisplayName(), VoteProposalFormatter.getStatusMessage(voteProposal, false, null));

                VoteBotProposalService.get().sendMessage(formatMessage(participant, newVote));

                if (!newVote)
                    mail(participant, false);

            }

        }

        //Send to wider distribution list if not recovered.
        if (newVote && !isRecovered())
            mail(escoPrivate, true);


    }


    private void sendResults() {

        logger.info("Results: {}", VoteProposalFormatter.getStatusMessage(voteProposal, false, null));

        for (Participant participant : voteProposal.getParticipants()) {


            VoteBotProposalService.get().sendMessage(formatMessage(participant, false));

            mail(participant, false);
        }


        mail(escoPrivate, false);


    }


    private void mail(Participant participant, boolean newVote) {

        if (Boolean.valueOf(System.getProperty(BotConfig.MAIL_SEND_EMAIL, "false"))) {


            new Thread(new MailerWorker(participant.getSymUser().getEmailAddress(),
                    System.getProperty(BotConfig.MAIL_VB_FROM),
                    "[VOTE][" + voteProposal.getVoteId() + "] " + voteProposal.getProposal().getName(),
                    VoteProposalFormatter.getMailStatusMessage(voteProposal, newVote, participant.getSymUser().getId())
            )).start();

        }
    }


    public boolean isRecovered() {
        return recovered;
    }

    public void setRecovered(boolean recovered) {
        this.recovered = recovered;
    }
}
