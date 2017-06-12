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


import org.symphonyoss.symphony.clients.model.SymUser;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by frank on 12/12/16.
 */
@XmlRootElement
public class VoteProposal extends VoteSession implements Serializable {

    private Set<Participant> participants;
    private int approvalPercentage;
    private long reminderFrequency;
    private Proposal proposal;
    private VoteResult result = VoteResult.ACTIVE;
    private boolean completed = false;
    private double yea;
    private double nay;
    private double abstain;
    private double totalCast;
    private List<VoteComment> comments = new ArrayList<>();


    public VoteProposal() {
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {

        //recalculate for final status
        update(false);

        if (result.equals(VoteResult.ACTIVE))
            result = VoteResult.FAILED;

        this.completed = completed;


    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    public int getApprovalPercentage() {
        return approvalPercentage;
    }

    public void setApprovalPercentage(int approvalPercentage) {
        this.approvalPercentage = approvalPercentage;
    }

    public long getReminderFrequency() {
        return reminderFrequency;
    }

    public void setReminderFrequency(long reminderFrequency) {
        this.reminderFrequency = reminderFrequency;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    public VoteResult getResult() {
        return result;
    }

    public void setResult(VoteResult result) {
        this.result = result;
    }

    public synchronized boolean castVote(Vote vote) {


        Participant participant = participants.stream().filter(p -> p.getSymUser().getId().equals(vote.getSymUser().getId())).findFirst().orElse(null);

        if (participant != null) {


            if (result.equals(VoteResult.ACTIVE)) {

                Vote currentVote = getVotes().stream().filter(v -> v.getSymUser().equals(vote.getSymUser())).findFirst().orElse(null);

                if (currentVote == null) {
                    //New vote
                    getVotes().add(vote);

                } else {
                    //Change of vote
                    currentVote.setVote(vote.getVote());
                }

                participant.setVoteCast(true);

                update(true);

                return true;
            }


        }

        return false;

    }

    public void update(boolean isActive) {

        yea = getVotes().stream().filter(vote -> vote.getVote() == VoteType.YEA).count();
        nay = getVotes().stream().filter(vote -> vote.getVote() == VoteType.NAY).count();
        abstain = getVotes().stream().filter(vote -> vote.getVote() == VoteType.ABSTAIN).count();

        totalCast = yea + nay + abstain;

        double totalRemain = getParticipants().size() - totalCast;
        double passedScore = (getParticipants().size() / 2.0) + 1;


        if (!isActive) {
            //Approved path
            if (passedScore <= yea) { //

                result = VoteResult.APPROVED;

                //Fail path
            } else if (nay + abstain > passedScore) {

                result = VoteResult.FAILED;

                //TIE
            } else if (yea == nay && totalRemain == 0) {

                result = VoteResult.TIE;


            }
        }


    }

    public double getNays() {
        return nay;

    }

    public double getYeas() {
        return yea;
    }

    public double getAbstains() {
        return abstain;
    }

    public double getTotalCast() {
        return totalCast;
    }

    public double getTotalRemain() {
        return getParticipants().size() - totalCast;
    }

    public void addParticipants(Set<SymUser> escoUsersVote) {

        if (participants == null)
            participants = new HashSet<>();

        for (SymUser symUser : escoUsersVote) {
            Participant participant = new Participant();
            participant.setSymUser(symUser);

            participants.add(participant);


        }


    }


    public List<VoteComment> getComments() {
        return comments;
    }

    public void setComments(List<VoteComment> comments) {
        this.comments = comments;
    }


}
