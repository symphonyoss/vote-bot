

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

package org.symphonyoss.vb;

import org.symphonyoss.vb.services.VoteBotProposalService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.vb.config.BotConfig;
import org.jolokia.jvmagent.JolokiaServer;
import org.jolokia.jvmagent.JolokiaServerConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frank on 9/4/2015.
 */
public class VoteBot {
    private static Logger logger = LoggerFactory.getLogger(VoteBot.class);


    public VoteBot() {

        BotConfig.Config.getProperty(BotConfig.ESCO_USER_VOTE);

        VoteBotProposalService voteBotProposalService = VoteBotProposalService.get();


        try {
            voteBotProposalService.init();
        } catch (Exception e) {
                logger.error("Could not init Esco Services..exiting. ", e);
                return;
        }


        Server server = new Server(BotConfig.Config.getInt("jetty.port",8080));


        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(BotConfig.Config.getString("jetty.contextpath","/symphony"));
        webapp.setWar(System.getProperty(BotConfig.WAR_FILE));
        logger.info("Using war file {}", System.getProperty(BotConfig.WAR_FILE));

        webapp.setClassLoader(Thread.currentThread().getContextClassLoader());
        webapp.setServerClasses(new String[]{"-org.eclipse.jetty.servlet.ServletContextHandler.Decorator"});
        server.setHandler(webapp);
        //webapp.addEventListener(new Listener());


        try {

            try {
                server.start();
            }catch(IllegalStateException e){

                logger.error("Could not start jetty server", e);
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error("Interrupt exception", e);
            }

            server.join();

            // Init Jolokja agent
            Map<String, String> serverConfigMap = new HashMap<>();
            // TODO - take it from SJC config
            serverConfigMap.put("JOLOKIA_HOST","*");
            JolokiaServerConfig serverConfig = new JolokiaServerConfig(serverConfigMap);
            JolokiaServer jolokiaServer = new JolokiaServer(serverConfig, true);
            jolokiaServer.start();
        } catch (Exception e) {

            e.printStackTrace();
            logger.error("Could not initialize Jetty server.");

        }
    }

    public static void main(String[] args) throws Exception {


        new VoteBot();

    }



}