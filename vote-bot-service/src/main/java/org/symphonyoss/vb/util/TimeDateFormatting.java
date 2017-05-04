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

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.symphonyoss.vb.constants.Constants.DATE_FORMAT;

/**
 * Created by frank on 3/25/17.
 */
public class TimeDateFormatting {

    public TimeDateFormatting() {
    }


    public static String formatDate(long time) {

        Date date = new Date(time);

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        return dateFormat.format(date);


    }


}
