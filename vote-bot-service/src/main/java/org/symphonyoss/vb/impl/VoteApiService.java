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

package org.symphonyoss.vb.impl;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.symphonyoss.vb.model.NotificationMessage;
import org.symphonyoss.vb.services.BotServices;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.io.Serializable;

/**
 * Created by Frank on 8/2/2015.
 */

@RequestScoped
@Service
public class VoteApiService implements Serializable {
    private Logger logger = LoggerFactory.getLogger(VoteApiService.class);



    BotServices botServices = BotServices.get();

    public Response publish(NotificationMessage notificationMessage) throws NotFoundException {
        JsonObject jo = new JsonObject();


        try{
            String response = botServices.publish(notificationMessage);
            jo.addProperty("message", response);
        }catch (Exception e) {

            jo.addProperty("message", e.getMessage());


            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(jo.toString())
                    .build();




        }



        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
       // jo.addProperty("message", "OK");

        return Response
                .status(Response.Status.OK)
                .entity(jo.toString())
                .build();



    }





}
