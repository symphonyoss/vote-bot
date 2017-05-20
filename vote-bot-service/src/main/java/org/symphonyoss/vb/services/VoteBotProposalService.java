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
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.common.MLTypes;
import org.symphonyoss.client.model.CacheType;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.client.services.ChatServiceListener;
import org.symphonyoss.client.services.ConnectionsService;
import org.symphonyoss.client.services.SymUserCache;
import org.symphonyoss.exceptions.MessagesException;
import org.symphonyoss.exceptions.SymException;
import org.symphonyoss.exceptions.UsersClientException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.vb.ai.VoteSessionProposalCommands;
import org.symphonyoss.vb.ai.commands.CommentsProposal;
import org.symphonyoss.vb.ai.commands.ListProposal;
import org.symphonyoss.vb.ai.commands.NewProposal;
import org.symphonyoss.vb.ai.commands.VotingProposal;
import org.symphonyoss.vb.config.BotConfig;
import org.symphonyoss.vb.constants.Constants;
import org.symphonyoss.vb.mail.MailReader;
import org.symphonyoss.vb.mail.MailReaderListener;
import org.symphonyoss.vb.mail.MailerWorker;
import org.symphonyoss.vb.model.VoteProposal;
import org.symphonyoss.vb.model.VoteResult;
import org.symphonyoss.vb.model.VoteSession;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by frank on 3/12/17.
 */
public class VoteBotProposalService implements ChatServiceListener, ChatListener, MailReaderListener {

    private BotServices botServices = BotServices.get();

    private String botUser = System.getProperty("bot.user", "esco.bot") + System.getProperty("bot.domain", "@markit.com");

    private static VoteBotProposalService self = new VoteBotProposalService();

    private SymphonyClient symphonyClient;

    private Set<SymUser> members = new HashSet<>();
    private Set<SymUser> membersWhoCanVote = new HashSet<>();

    private ConcurrentHashMap<Long, Chat> memberChats = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer, VoteProposal> archiveVotingProposals = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, VoteProposal> activeVotingProposals = new ConcurrentHashMap<>();


    private final static Logger logger = LoggerFactory.getLogger(VoteBotProposalService.class);

    private int lastVoteId = 0;


    public VoteBotProposalService() {


    }

    public static VoteBotProposalService get() {
        return self;
    }

    public void init() throws Exception {

        try {

            symphonyClient = botServices.getSymClientByUser(botUser);
            symphonyClient.getChatService().addListener(this);

            ConnectionsService connectionsService = new ConnectionsService(symphonyClient);
            connectionsService.setAutoAccept(true);


            loadAllChats();
            loadFromCache();
            MailReader mailReader = new MailReader(symphonyClient);
            mailReader.addListener(this);
            mailReader.startPoll();


        } catch (Exception e) {
            logger.error("Failed to init symphony client for VoteBotProposalService", e);
        }
    }


    @Override
    public void onChatMessage(SymMessage symMessage) {

        logger.info("New chat message {} {}", symMessage.getFromUserId(), symMessage.getMessage());


        Chat memberChat = memberChats.get(symMessage.getFromUserId());

        //Only respond to esco users
        if (memberChat == null)
            return;


        String[] chunks = symMessage.getMessageText().replace("\u00a0", " ").replace("&nbsp;", " ").split("\\s+");
        String command = chunks[0].trim().toLowerCase();


        //check for command
        if ( command.length() <= 0 || command.charAt(0) == '/' && !VoteSessionProposalCommands.isCommand(command)) {
            replyUsage(symMessage);
            return;

        }


        try {

            if (command.equals(Constants.NEW_PROPOSAL_COMMAND) && chunks.length >= 3) {


                if (!new NewProposal(this, symMessage).exec()) {
                    replyUsage(symMessage);
                }

            } else if (command.equals(Constants.VOTE_COMMAND)) {


                if (!new VotingProposal(this, symMessage).exec())
                    replyUsage(symMessage);

            } else if (command.equals(Constants.LIST_VOTE_COMMAND) || command.equals(Constants.HELP_COMMAND)) {

                if (!new ListProposal(this, symMessage).exec())
                    replyUsage(symMessage);


            } else if (command.equals(Constants.COMMENT_VOTE_COMMAND)) {

                if (!new CommentsProposal(this, symMessage).exec())
                    replyUsage(symMessage);


            } else if (command.equals(Constants.USAGE_COMMAND) || command.equals(Constants.HELP_COMMAND)) {


                symMessage.setMessage(Constants.USAGE);
                symphonyClient.getMessageService().sendMessage(memberChat, symMessage);


            } else {


                relayMessage(symMessage);

            }

        } catch (
                SymException e)

        {

            logger.error("Error occurred processing incoming message.", e);


        }

    }


    @Override
    public void onNewChat(Chat chat) {

    }

    @Override
    public void onRemovedChat(Chat chat) {

    }


    private void loadAllChats() {

        logger.info("EscoUserList: {}", System.getProperty(BotConfig.ESCO_USER_LIST));
        logger.info("EscoUserVote: {}", System.getProperty(BotConfig.ESCO_USER_VOTE));


        String[] escoUserList = System.getProperty(BotConfig.ESCO_USER_LIST).split(",");
        String[] escoUserVote = System.getProperty(BotConfig.ESCO_USER_VOTE).split(",");

        for (String entry : escoUserList) {

            SymUser symUser = getSymUser(entry);

            if (symUser != null && !members.contains(symUser)) {

                addChat(symUser);
                members.add(symUser);


                logger.info("Loaded esco user {} {} {}", symUser.getDisplayName(), symUser.getId(), symUser.getEmailAddress());
            }

        }


        for (String entry : escoUserVote) {

            SymUser symUser = getSymUser(entry);

            if (symUser != null && !membersWhoCanVote.contains(symUser)) {
                membersWhoCanVote.add(symUser);

                addChat(symUser);

                logger.info("Loaded esco voting user {} {} {}", symUser.getDisplayName(), symUser.getId(), symUser.getEmailAddress());

            }

        }
    }


    private void addChat(SymUser symUser) {

        if (memberChats.get(symUser.getId()) == null) {

            Chat chat = new Chat();
            chat.setLocalUser(symphonyClient.getLocalUser());
            Set<SymUser> remoteUsers = new HashSet<>();
            remoteUsers.add(symUser);
            chat.setRemoteUsers(remoteUsers);
            chat.addListener(this);


            if (symUser.getId() >= 0 && symphonyClient.getChatService().addChat(chat)) {
                memberChats.put(symUser.getId(), chat);

                logger.info("Added chat for esco vote user {} {} {}", symUser.getDisplayName(), symUser.getId(), symUser.getEmailAddress());

            } else if (symUser.getId() < 0) {
                logger.info("Added chat for email esco profile for user {}: {}", symUser.getId(), symUser.getDisplayName());

                memberChats.put(symUser.getId(), chat);
            } else {

                logger.error("Failed to add chat for esco profile for user {}: {}", symUser.getId(), symUser.getDisplayName());

            }


        }
    }


    private SymUser getSymUser(String entry) {

        String[] user = entry.split(":");
        if (user.length == 3) {

            SymUser symUser = new SymUser();

            if (user[0].equals(Constants.EMAIL_FLAG)) {

                long id = (long) user[2].toLowerCase().hashCode();
                if (id >= 0)
                    id = id * -1;

                //-neg number from email hash will be the user id.
                symUser.setId(id);

            } else {
                symUser.setId(new Long(user[0]));
            }
            symUser.setDisplayName(user[1]);
            symUser.setEmailAddress(user[2].toLowerCase());

            return symUser;

        }

        return null;
    }

    private void replyUsage(SymMessage symMessage) {

        symMessage.setMessage(Constants.USAGE);

        replyToMessage(symMessage);

    }

    public void replyToMessage(SymMessage symMessage) {


        if (symMessage.getSymUser() == null) {
            SymUser symUser = new SymUser();
            symMessage.setSymUser(symUser);
        }

        symMessage.getSymUser().setId(symMessage.getFromUserId());

        sendMessage(symMessage);


    }

    public void relayMessage(SymMessage symMessage) {


        SymUser symUser = members.stream().filter(symUser1 -> symUser1.getId().equals(symMessage.getFromUserId())).findFirst().orElse(null);

        if(symUser == null)
            return;

        String message = symMessage.getMessage();


        symMessage.setMessage(message.replaceFirst(">", ">  " + MLTypes.START_BOLD + symUser.getDisplayName() + ": " + MLTypes.END_BOLD));


        memberChats.forEach((key, value) -> {

            try {
                //Relay to everyone but the sending user
                if (value.getStreamId() != null && !key.equals(symUser.getId())) {
                    symphonyClient.getMessageService().sendMessage(value, symMessage);

                } else if (key < 0 && !key.equals(symUser.getId())) {

                    sendEmail(value.getRemoteUsers().iterator().next(), symMessage);


                } else {
                    logger.info("Ignoring relay to stream {}: {}", value.getStreamId(), value.getRemoteUsers().iterator().next().getDisplayName());
                }
            } catch (MessagesException e) {
                e.printStackTrace();
            }

        });


    }

    public synchronized void sendMessage(SymMessage symMessage) {

        Chat chat = memberChats.get(symMessage.getSymUser().getId());

        logger.info("Sending message to user {}:{}", symMessage.getSymUser().getId(), symMessage.getSymUser().getEmailAddress());

        if (chat == null)
            return;

        try {
            //Email?
            if (symMessage.getSymUser().getId() < 0) {

                sendEmail(symMessage.getSymUser(), symMessage);

            } else {
                symphonyClient.getMessageService().sendMessage(chat, symMessage);
            }
        } catch (Exception e) {
            logger.error("Could not send message to " + chat.getRemoteUsers().iterator().next().getDisplayName(), e);
        }

    }


    public void archiveVotingProposal(int voteId) {

        archiveVotingProposals.put(voteId, activeVotingProposals.get(voteId));
        activeVotingProposals.remove(voteId);

    }

    private void loadFromCache() throws Exception {


        Set<VoteSession> voteSessions = JsonPersist.getVoteSessions();

        for (VoteSession voteSession : voteSessions) {

            VoteProposal voteProposal = (VoteProposal) voteSession;

            //need to increment vote id to last known.
            if (lastVoteId < voteProposal.getVoteId())
                lastVoteId = voteProposal.getVoteId();

            if (voteProposal.getResult().equals(VoteResult.ACTIVE)) {


                activeVotingProposals.put(voteProposal.getVoteId(), voteProposal);
                VoteProposalWorker voteProposalWorker = new VoteProposalWorker(voteProposal);

                voteProposalWorker.setRecovered(true);


                new Thread(voteProposalWorker).start();

            } else {

                archiveVotingProposals.put(voteProposal.getVoteId(), voteProposal);

            }


        }


    }


    public Set<SymUser> getMembers() {
        return members;
    }

    public void setMembers(Set<SymUser> members) {
        this.members = members;
    }

    public Set<SymUser> getMembersWhoCanVote() {
        return membersWhoCanVote;
    }

    public void setMembersWhoCanVote(Set<SymUser> membersWhoCanVote) {
        this.membersWhoCanVote = membersWhoCanVote;
    }

    public ConcurrentHashMap<Long, Chat> getMemberChats() {
        return memberChats;
    }

    public void setMemberChats(ConcurrentHashMap<Long, Chat> memberChats) {
        this.memberChats = memberChats;
    }

    public ConcurrentHashMap<Integer, VoteProposal> getArchiveVotingProposals() {
        return archiveVotingProposals;
    }

    public void setArchiveVotingProposals(ConcurrentHashMap<Integer, VoteProposal> archiveVotingProposals) {
        this.archiveVotingProposals = archiveVotingProposals;
    }

    public ConcurrentHashMap<Integer, VoteProposal> getActiveVotingProposals() {
        return activeVotingProposals;
    }

    public void setActiveVotingProposals(ConcurrentHashMap<Integer, VoteProposal> activeVotingProposals) {
        this.activeVotingProposals = activeVotingProposals;
    }

    public int getLastVoteId() {
        return lastVoteId;
    }

    public void setLastVoteId(int lastVoteId) {
        this.lastVoteId = lastVoteId;
    }


    public synchronized int getNextVoteId() {
        return ++lastVoteId;
    }

    @Override
    public void newMailMessage(SymMessage symMessage) {

        if (symMessage.getFormat() == SymMessage.Format.TEXT) {
            symMessage.setMessage(MLTypes.START_ML.toString() + symMessage.getMessage() + MLTypes.END_ML);
            symMessage.setFormat(SymMessage.Format.MESSAGEML);
        }


        onChatMessage(symMessage);

    }

    private void sendEmail(SymUser symUser, SymMessage symMessage) {

        if (symUser == null || symMessage == null)
            return;

        logger.info("Sending email message to user {}:{}:{}", symUser.getId(), symUser.getEmailAddress(), symMessage.getMessageText());

        new Thread(new MailerWorker(
                symUser.getEmailAddress(),
                System.getProperty(BotConfig.MAIL_VB_FROM),
                "[ESCO BOT RELAY]",
                mlToText(symMessage.getMessage())

        )).start();

    }


    private String mlToText(String messageMl) {
        String textMessage = "";

        textMessage = messageMl.substring(messageMl.indexOf(">") + 1, messageMl.lastIndexOf("<"));

        textMessage = textMessage.replaceAll(MLTypes.BREAK.toString(), "\n");
        textMessage = textMessage.replaceAll(MLTypes.START_BOLD.toString(), "");
        textMessage = textMessage.replaceAll(MLTypes.END_BOLD.toString(), "");
        textMessage = textMessage.replaceAll("<mention ", "");
        textMessage = textMessage.replaceAll("<hash ", "");
        textMessage = textMessage.replaceAll("&nbsp;", " ");

        while (textMessage.contains("uid=\"")) {

            String uid = textMessage.substring(textMessage.indexOf("uid=") + 5, textMessage.indexOf("\"/>"));


            SymUser symUser = members.stream().filter(symUser1 -> symUser1.getId().equals(Long.valueOf(uid))).findFirst().orElse(null);

            if(symUser == null){

                try {
                    symUser = ((SymUserCache)symphonyClient.getCache(CacheType.USER)).getUserById(Long.valueOf(uid));
                } catch (UsersClientException e) {
                    logger.error("Could not find email by UID: {}", uid,e);
                    symUser = new SymUser();
                    symUser.setEmailAddress("UNKNOWN@EMAIL");
                }

            }

            textMessage = textMessage.replaceAll("uid=\"" + uid + "\"/>", symUser.getEmailAddress());


        }

        while (textMessage.contains("tag=\"")) {

            String tag = textMessage.substring(textMessage.indexOf("tag=") + 5, textMessage.indexOf("\"/>"));

            textMessage = textMessage.replaceAll("tag=\"" + tag + "\"/>", "#" + tag);


        }


        textMessage = textMessage.replaceAll(":\\+1:", "Yea");
        textMessage = textMessage.replaceAll(":thumbsdown:", "Nay");
        textMessage = textMessage.replaceAll(":worried:", "Abstain");


        return textMessage;
    }


}
