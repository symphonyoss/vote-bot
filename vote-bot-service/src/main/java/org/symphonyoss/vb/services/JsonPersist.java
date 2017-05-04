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

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.vb.config.BotConfig;
import org.symphonyoss.vb.model.VoteProposal;
import org.symphonyoss.vb.model.VoteSession;
import org.symphonyoss.vb.util.AwsS3Client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by frank on 3/17/17.
 */
public class JsonPersist {

    private static final Logger logger = LoggerFactory.getLogger(JsonPersist.class);


    static {

        if (!new File(System.getProperty("files.json")).isDirectory())
            new File(System.getProperty("files.json")).mkdirs();

    }

    public static synchronized void writeVoteSession(VoteSession voteSession) throws Exception {

        String fileName =  voteSession.getVoteId() + ".json";

        try {
            Gson gson = new Gson();


            FileWriter jsonFile = new FileWriter(System.getProperty("files.json") + fileName);


            gson.toJson(voteSession, jsonFile);
            jsonFile.flush();
            jsonFile.close();


            if (Boolean.parseBoolean(System.getProperty(BotConfig.S3_ENABLED))) {

                AwsS3Client awsS3Client = new AwsS3Client();
                awsS3Client.putObject(
                        System.getProperty(BotConfig.S3_BUCKET),
                        System.getProperty(BotConfig.VB_S3_PREFIX_JSON)  + fileName,
                        new ByteArrayInputStream(gson.toJson(voteSession).getBytes(StandardCharsets.UTF_8)),
                        null);


            }

        } catch (IOException e) {
            logger.error("Could not write file for voteId {}", voteSession.getVoteId(), e);
            throw (e);
        }

    }

    public static boolean removeHashtagFile(VoteSession voteSession) {


        return new File(System.getProperty("files.json") + voteSession.getVoteId() + ".json").delete();


    }


    public static Set<VoteSession> getVoteSessions() throws Exception {

        Set<VoteSession> voteSessions = new HashSet<>();
        Gson gson = new Gson();

        if (Boolean.parseBoolean(System.getProperty(BotConfig.S3_ENABLED))) {

            AwsS3Client awsS3Client = new AwsS3Client();
            List<S3ObjectSummary> allObjects = awsS3Client.getAllObjects(System.getProperty(BotConfig.S3_BUCKET), System.getProperty(BotConfig.VB_S3_PREFIX_JSON));

            for (S3ObjectSummary objectSummary : allObjects) {

                if (!objectSummary.getKey().contains(".json"))
                    continue;

                logger.info("Loading vote session from aws cache [s3://{}/{}]", objectSummary.getBucketName(), objectSummary.getKey());

                VoteProposal voteProposal = gson.fromJson(new InputStreamReader(awsS3Client.getObject(objectSummary)), VoteProposal.class);
                voteSessions.add(voteProposal);


            }

        } else {
            File[] files = new File(System.getProperty("files.json")).listFiles();

            if (files == null) {
                logger.error("Failed to load locate directory [{}] for json pre-load..exiting", System.getProperty("files.json"));
                System.exit(1);
            }


            for (File file : files) {

                if (!file.getName().contains(".json"))
                    continue;

                logger.info("Loading vote session from cache [{}]", file.getName());
                try {
                    VoteProposal voteProposal = gson.fromJson(new FileReader(file), VoteProposal.class);
                    voteSessions.add(voteProposal);


                } catch (IOException e) {
                    logger.error("Could not load json {} ", file.getName(), e);
                    throw (e);
                }
            }


        }
        return voteSessions;

    }


}
