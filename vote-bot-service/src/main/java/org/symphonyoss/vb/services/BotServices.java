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


import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.impl.CustomHttpClient;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.RoomService;
import org.symphonyoss.exceptions.SymException;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.pod.model.Stream;
import org.symphonyoss.symphony.pod.model.UserIdList;
import org.symphonyoss.vb.model.NotificationMessage;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Simple example of the RoomService.
 * <p>
 * It will send a message to a room through from a stream (property: room.stream)
 * This will create a Room object, which is populated with all room attributes and
 * membership.  Adding a listener, will provide callbacks.
 * <p>
 * <p>
 * <p>
 * REQUIRED VM Arguments or System Properties:
 * <p>
 * -Dsessionauth.url=https://pod_fqdn:port/sessionauth
 * -Dkeyauth.url=https://pod_fqdn:port/keyauth
 * -Dsymphony.agent.pod.url=https://agent_fqdn:port/pod
 * -Dsymphony.agent.agent.url=https://agent_fqdn:port/agent
 * -Dcerts.dir=/dev/certs/
 * -Dkeystore.password=(Pass)
 * -Dtruststore.file=/dev/certs/server.truststore
 * -Dtruststore.password=(Pass)
 * -Dbot.user=bot.user1
 * -Dbot.domain=@domain.com
 * -Duser.call.home=frank.tarsillo@markit.com
 * -Droom.stream=(Stream)
 * <p>
 * <p>
 * <p>
 * <p>
 * Created by Frank Tarsillo on 5/15/2016.
 */
@ApplicationScoped
@Service
public class BotServices {


    private final Logger logger = LoggerFactory.getLogger(BotServices.class);

    private RoomService roomService;

    private static BotServices self = new BotServices();

    private ConcurrentHashMap<String, SymphonyClient> symClients = new ConcurrentHashMap<>();

    public BotServices() {


    }

    public static BotServices get() {
        return self;
    }

    public static void main(String[] args) {


        new BotServices();

    }

    public SymphonyClient getSymClientByUser(String fromUser) throws Exception{
        SymphonyClient symphonyClient = symClients.get(fromUser);

        return symphonyClient == null?init(fromUser):symphonyClient;

    }

    private SymphonyClient init(String fromUser) throws Exception {

        logger.info("FROM USER: {}", fromUser);

        String userName = fromUser;

        if (fromUser.contains("@")) {
            userName = fromUser.substring(0, fromUser.indexOf("@"));
        }


        //Create a basic client instance.
        SymphonyClient symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

        logger.debug("{} {}", System.getProperty("sessionauth.url"),
                System.getProperty("keyauth.url"));


        try {
            Client httpClient = CustomHttpClient.getClient(
                    System.getProperty("certs.dir") + userName + ".p12",
                    System.getProperty("keystore.password"),
                    System.getProperty("truststore.file"),
                    System.getProperty("truststore.password"));
            symClient.setDefaultHttpClient(httpClient);
        } catch (Exception e) {
            logger.error("Failed to create custom http client", e);
            return null;
        }


        //Init the Symphony authorization client, which requires both the key and session URL's.  In most cases,
        //the same fqdn but different URLs.
        AuthorizationClient authClient = new AuthorizationClient(
                System.getProperty("sessionauth.url"),
                System.getProperty("keyauth.url"),
                symClient.getDefaultHttpClient());


        //Create a SymAuth which holds both key and session tokens.  This will call the external service.
        SymAuth symAuth = authClient.authenticate();


        //With a valid SymAuth we can now init our client.
        symClient.init(
                symAuth,
                fromUser,
                System.getProperty("symphony.agent.agent.url"),
                System.getProperty("symphony.agent.pod.url")
        );


        symClients.put(fromUser, symClient);

        return symClient;


    }


    //Chat sessions callback method.
    public void onChatMessage(SymMessage message) {
        if (message == null)
            return;

        logger.debug("TS: {}\nFrom ID: {}\nMessage: {}\nMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());


    }






    public String publish(NotificationMessage notificationMessage) throws Exception {

        SymphonyClient symClient;

        StringBuilder response = new StringBuilder();

        if (notificationMessage.getBody() == null)
            throw new Exception("Notification body is null..");

        if (notificationMessage.getFrom() == null)
            throw new Exception("No from user was specified..");

        symClient = symClients.get(notificationMessage.getFrom());


        if (symClient == null) {

            try {
                symClient = init(notificationMessage.getFrom());
            }catch(Exception e){

                logger.error("Could not init from BOT user {}",notificationMessage.getFrom(), e);
                throw(e);
            }

        }

        SymMessage message = new SymMessage();
        //message.setStreamId(notificationMessage.getToStreams());
        if (notificationMessage.getType().equals("MESSAGEML")) {
            message.setFormat(SymMessage.Format.MESSAGEML);
        } else {
            message.setFormat(SymMessage.Format.TEXT);
        }


        message.setMessage(notificationMessage.getBody());


        if (notificationMessage.getToStreams() != null) {
            String[] streams = notificationMessage.getToStreams().split(",");

            for (String stream : streams) {
                Stream s = new Stream();
                s.setId(stream.trim());
                symClient.getMessagesClient().sendMessage(s, message);
            }
        }

        if (notificationMessage.getToUsers() != null) {
            String[] users = notificationMessage.getToUsers().split(",");

            for (String user : users) {
                try {
                    if (user.contains("@")) {
                        symClient.getMessageService().sendMessage(user.trim(), message);
                    } else {

                        UserIdList userIdList = new UserIdList();
                        userIdList.add(Long.valueOf(user));
                        Stream stream = symClient.getStreamsClient().getStream(userIdList);
                        symClient.getMessagesClient().sendMessage(stream, message);
                    }

                    response.append("{");
                    response.append(user);
                    response.append(":");
                    response.append("200");
                    response.append("},");


                } catch (SymException e) {


                    response.append("{");
                    response.append(user);
                    response.append(":");
                    response.append("500");
                    response.append("},");

                }
            }


        }

        return response.toString();

    }

}
