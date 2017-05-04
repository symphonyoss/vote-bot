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



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.vb.config.BotConfig;


import java.util.TimerTask;

/**
 * Poll for new mail files
 *
 * @author Frank Tarsillo on 4/16/17.
 */
public class MailReaderWorker extends TimerTask{


    private Logger logger = LoggerFactory.getLogger(MailReaderWorker.class);

    MailReader mailReader;

    public MailReaderWorker(MailReader mailReader){
        this.mailReader = mailReader;

    }

    @Override
    public void run() {

        logger.info("Checking for new mail messages...");
        mailReader.newMessages(mailReader.getMessages(System.getProperty(BotConfig.S3_BUCKET), System.getProperty(BotConfig.MAIL_S3_PREFIX_INCOMING)));


    }




}
