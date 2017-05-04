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

package org.symphonyoss.vb.ai;


import org.symphonyoss.vb.constants.Constants;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by frank on 3/14/17.
 */
public class VoteSessionProposalCommands {
    private final static Set<String> commands = new HashSet<>();

    static{

        commands.add(Constants.CANCEL_VOTE_COMMAND);
        commands.add(Constants.VOTE_COMMAND);
        commands.add(Constants.LIST_VOTE_COMMAND);
        commands.add(Constants.NEW_PROPOSAL_COMMAND);
        commands.add(Constants.USAGE_COMMAND);
        commands.add(Constants.COMMENT_VOTE_COMMAND);


    }


    public static boolean isCommand(String command){

        return commands.contains(command);


    }


    public static Set<String> getCommands() {
        return commands;
    }


}
