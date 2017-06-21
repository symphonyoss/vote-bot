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

package org.symphonyoss.vb.mail;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.CacheType;
import org.symphonyoss.client.services.SymUserCache;
import org.symphonyoss.client.exceptions.SymException;
import org.symphonyoss.client.exceptions.UsersClientException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.vb.config.BotConfig;
import org.symphonyoss.vb.constants.Constants;
import org.symphonyoss.vb.services.BotServices;
import org.symphonyoss.vb.util.AwsS3Client;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Will read incoming mail
 *
 * @author Frank Tarsillo on 4/15/17.
 */
public class MailReader {


    private final Logger logger = LoggerFactory.getLogger(MailReader.class);
    private final SymphonyClient symClient;
    private final Long readPollTime = Long.valueOf(System.getProperty(Constants.MAIL_READER_POLL_SLEEP, "30000"));
    private final Set<MailReaderListener> mailReaderListeners = ConcurrentHashMap.newKeySet();
    final TimerTask mailReaderWorker = new MailReaderWorker(this);
    final Timer timer = new Timer(true);


    public MailReader(SymphonyClient symClient) {

        this.symClient = symClient;


    }

    public void startPoll() {

        timer.scheduleAtFixedRate(mailReaderWorker, 0, readPollTime);
    }

    public void stopPoll() {
        timer.cancel();
    }

    public void addListener(MailReaderListener listener) {

        mailReaderListeners.add(listener);

    }


    public static void main(String[] args) {

        SymphonyClient symClient;
        try {
            symClient = BotServices.get().getSymClientByUser("vote.bot@markit.com");

            MailReader mailReader = new MailReader(symClient);
            mailReader.startPoll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<SymMessage> getMessages(String bucket, String prefix) {

        AwsS3Client awsS3Client = new AwsS3Client();

        List<SymMessage> messages = new ArrayList<>();


        List<S3ObjectSummary> objectSummaries = awsS3Client.getAllObjects(System.getProperty(BotConfig.S3_BUCKET), System.getProperty(BotConfig.MAIL_S3_PREFIX_INCOMING));

        for (S3ObjectSummary objectSummary :
                objectSummaries) {


            if (objectSummary.getKey().equals(prefix))
                continue;

            logger.info("New mail file: {}:{}:{}", objectSummary.getKey(), objectSummary.getSize(), objectSummary.getLastModified());


            try {

                Message message = Mailer.getMessage(awsS3Client.getObject(objectSummary));

                //Couldn't convert it.
                if (message == null)
                    continue;

                try {

                    SymMessage symMessage = getSymMessage(message);

                    if (symMessage != null) {

                        logger.info("New mail message: from: {}, subject: {}, body: {}", symMessage.getSymUser().getEmailAddress(), message.getSubject(), symMessage.getMessage());
                        messages.add(symMessage);
                    }

                } catch (SymException ex) {

                    logger.error("Could not convert email to SymMessage from file [{}]", objectSummary.getKey(), ex);


                }

                String destKey = objectSummary.getKey().substring(objectSummary.getKey().lastIndexOf("/") + 1);

                //logger.info("DEST FILE: {}", destKey);

                awsS3Client.moveObject(objectSummary, System.getProperty(BotConfig.S3_BUCKET), System.getProperty(BotConfig.MAIL_S3_PREFIX_PROCESSED) + destKey);

            } catch (Exception e) {
                logger.error("Failed to process incoming email [{}]", objectSummary.getKey(), e);
            }


        }

        return messages;
    }


    private void displayTextInputStream(InputStream input)
            throws IOException {

        if (input == null) {
            logger.error("InputStream was null..");
            return;
        }
        // Read one text line at a time and display.
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            logger.info("    {}", line);
        }
        logger.info("");
    }


    private SymMessage getSymMessage(Message message) throws SymException {
        try {
            if (message.getFrom() == null || message.getRecipients(Message.RecipientType.TO) == null || message.getSubject() == null)
                throw new SymException("Mail message is malformed");

            SymMessage symMessage = new SymMessage();

            String messageText = "";

            messageText = getMessageText(message, messageText);

            //First new line is considered <Enter> for now.
            if (messageText.contains("\n"))
                messageText = messageText.substring(0, messageText.indexOf("\n")).trim();

            logger.info("MessageText: {}", messageText);

            if (messageText.length() == 0)
                throw new SymException("Mail message contained no body");

            symMessage.setMessage(messageText);
            symMessage.setFormat(SymMessage.Format.TEXT);

            Address[] froms = message.getFrom();


            String email = froms == null ? null : ((InternetAddress) froms[0]).getAddress().toLowerCase();

            if (froms == null)
                return null;


            for (Address a : froms) {
                logger.info("ADDRESS: {}:{}", a.toString(), email);
            }


            SymUser symUser;
            try {
                //All we care about is the first user..
                symUser = ((SymUserCache) symClient.getCache(CacheType.USER)).getUserByEmail(email);
                symMessage.setFromUserId(symUser.getId());

                if (symUser.getEmailAddress() == null)
                    symUser.setEmailAddress(email);

            } catch (UsersClientException e) {
                logger.debug("Could not find user {}", email);

                symUser = new SymUser();

                long id = (long) email.hashCode();

                if (id >= 0)
                    id = id * -1;

                //Sneaky but consistent
                symUser.setId(id);
                symUser.setEmailAddress(email);

            }

            symMessage.setSymUser(symUser);


            return symMessage;


        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }


    private String getMessageText(Part p, String messageText) throws MessagingException {


        try {
            //check if the content is plain text
            if (p.isMimeType("text/plain")) {

                messageText += (String) p.getContent();

                //  logger.info("Adding message: {}", messageText);
            }
            //check if the content has attachment
            else if (p.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) p.getContent();
                int count = mp.getCount();

                for (int i = 0; i < count; i++)
                    messageText = getMessageText(mp.getBodyPart(i), messageText);


            }
        } catch (IOException e) {

            throw new MessagingException("Could not read content from message..", e);
        }

        return messageText;

    }


    public void newMessages(List<SymMessage> symMessages) {

        if (symMessages != null)
            for (SymMessage symMessage : symMessages) {

                for (MailReaderListener mailReaderListener : mailReaderListeners)
                    mailReaderListener.newMailMessage(symMessage);

            }

    }


}


