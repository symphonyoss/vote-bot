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

package org.symphonyoss.vb.util;

import org.symphonyoss.symphony.clients.model.SymRoomDetail;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.vb.constants.Constants;
import org.symphonyoss.vb.mail.MailerWorker;
import org.symphonyoss.vb.services.BotServices;
import org.symphonyoss.client.SymphonyClient;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by frank on 3/12/17.
 */
public class RunSomething {

    public RunSomething() {


//
//      new MailerWorker("frank.tarsillo@markit.com", "esco.bot@esco.mdevlab.com", "THIS IS A BOT TEST", "BODY").run();
//
//        System.exit(1);
//
        BotServices botServices = BotServices.get();

        SymphonyClient symphonyClient;
//
//
        try {
            symphonyClient= botServices.getSymClientByUser("vote.bot@markit.com");


           // SymUser symUser = symphonyClient.getUsersClient().getUserFromId(new Long("70781061038247"));


            SymRoomDetail symRoomDetail = symphonyClient.getStreamsClient().getRoomDetail("0s9TJ4KC8jAKL19cdtUTuH___qzqCPu7dA");


            System.out.println("This room is: " + symRoomDetail.getRoomAttributes().getCopyProtected());

            symRoomDetail.getRoomAttributes().setCopyProtected(false);

            symphonyClient.getStreamsClient().updateChatRoom("0s9TJ4KC8jAKL19cdtUTuH___qzqCPu7dA", symRoomDetail.getRoomAttributes());


            System.out.println("This room is now: " + symphonyClient.getStreamsClient().getRoomDetail("0s9TJ4KC8jAKL19cdtUTuH___qzqCPu7dA").getRoomAttributes().getCopyProtected());



//        SymUserConnection symUserConnection = new SymUserConnection();
//
//
//        symUserConnection.setUserId(new Long("69956427334318"));

        //symphonyClient.getConnectionsClient().sendConnectionRequest(new SymUserConnectionRequest(symUserConnection));

//        symphonyClient.getConnectionsClient().acceptConnectionRequest(new SymUserConnectionRequest(symUserConnection));




        System.out.println("Finished sending connection request...");
        symphonyClient.shutdown();

        System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args){

        new RunSomething();
    }


}
