
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

package org.symphonyoss.vb.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public final class BotConfig {

    public static final Configuration Config;
    private final static Logger logger = LoggerFactory.getLogger(BotConfig.class);


    //Properties
    public final static String CONFIG_DIR = "bot.config.dir";
    public final static String CONFIG_FILE = "bot.properties";
    public final static String WAR_FILE = "war.file";
    public final static String MCP_NOTIFICATION_USER = "mcp.notification.user";
    public final static String MCP_NOTIFICATION_PASSWORD = "mcp.notification.password";
    public final static String KEYSTORE_PASSWORD = "keystore.password";
    public final static String TRUSTSTORE_PASSWORD = "truststore.password";
    public final static String SESSIONAUTH_URL = "sessionauth.url";
    public final static String KEYAUTH_URL = "keyauth.url";
    public final static String SYMPHONY_POD = "symphony.agent.pod.url";
    public final static String SYMPHONY_AGENT = "symphony.agent.agent.url";
    public final static String CERTS_DIR = "certs.dir";
    public final static String TRUSTSTORE_FILE = "truststore.file";
    public final static String BOT_USER = "bot.user";
    public final static String BOT_DOMAIN = "bot.domain";
    public final static String ADMIN_USER = "admin.user";
    public final static String FILES_JSON = "files.json";
    public final static String ESCO_USER_LIST = "esco.user.list";
    public final static String ESCO_USER_VOTE = "esco.user.vote";
    public static final String API_USER = "api.user";
    public static final String API_PASSWORD = "api.password";
    public final static String MAIL_SMTP_HOST = "mail.smtp.host";
    public final static String MAIL_SMTP_USER = "mail.smtp.user";
    public final static String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    public final static String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public final static String MAIL_SMTP_PORT = "mail.smtp.port";
    public final static String MAIL_SMTP_DEBUG = "mail.smtp.debug";
    public final static String MAIL_VB_FROM = "mail.vb.from";
    public final static String MAIL_SEND_EMAIL = "mail.send.email";
    public final static String MAIL_DISTRO_EMAIL = "mail.distro.email";
    public final static String MAIL_S3_PREFIX_INCOMING = "mail.s3.prefix.incoming";
    public final static String MAIL_S3_PREFIX_PROCESSED = "mail.s3.prefix.processed";
    public final static String S3_ENABLED = "s3.enabled";
    public final static String S3_ACCESS_KEY = "s3.access.key";
    public final static String S3_KEY_ID = "s3.key.id";
    public final static String S3_BUCKET = "s3.bucket";
    public final static String VB_S3_PREFIX_JSON = "vb.s3.prefix.json";


    //Env
    public final static String KEYSTORE_PASSWORD_ENV = "KEYSTORE_PASSWORD";
    public final static String TRUSTSTORE_PASSWORD_ENV = "TRUSTSTORE_PASSWORD";
    public final static String SESSIONAUTH_URL_ENV = "SESSION_AUTH";
    public final static String KEYAUTH_URL_ENV = "KEY_AUTH";
    public final static String SYMPHONY_POD_ENV = "SYMPHONY_POD";
    public final static String SYMPHONY_AGENT_ENV = "SYMPHONY_AGENT";
    public final static String CERTS_DIR_ENV = "CERTS";
    public final static String TRUSTSTORE_FILE_ENV = "TRUSTSTORE_FILE";
    public final static String BOT_USER_ENV = "BOT_USER";
    public final static String BOT_DOMAIN_ENV = "BOT_DOMAIN";
    public final static String MCP_NOTIFICATION_USER_ENV = "MCP_NOTIFICATION_USER";
    public final static String MCP_NOTIFICATION_PASSWORD_ENV = "MCP_NOTIFICATION_PASSWORD";
    public final static String ESCO_USER_LIST_ENV = "ESCO_USER_LIST";
    public final static String ESCO_USER_VOTE_ENV = "ESCO_USER_VOTE";
    public final static String FILES_JSON_ENV = "FILES_JSON";
    public static final String API_USER_ENV = "API_USER";
    public static final String API_PASSWORD_ENV = "API_PASSWORD";
    public final static String MAIL_SMTP_HOST_ENV = "MAIL_SMTP_HOST";
    public final static String MAIL_SMTP_USER_ENV = "MAIL_SMTP_USER";
    public final static String MAIL_SMTP_PASSWORD_ENV = "MAIL_SMTP_PASSWORD";
    public final static String MAIL_SMTP_AUTH_ENV = "MAIL_SMTP_AUTH";
    public final static String MAIL_SMTP_PORT_ENV = "MAIL_SMTP_PORT";
    public final static String MAIL_SMTP_DEBUG_ENV = "MAIL_SMTP_DEBUG";
    public final static String MAIL_VB_FROM_ENV = "MAIL_VB_FROM";
    public final static String MAIL_SEND_EMAIL_ENV = "MAIL_SEND_EMAIL";
    public final static String MAIL_DISTRO_EMAIL_ENV = "MAIL_DISTRO_EMAIL";
    public final static String MAIL_S3_PREFIX_INCOMING_ENV = "MAIL_S3_PREFIX_INCOMING";
    public final static String MAIL_S3_PREFIX_PROCESSED_ENV = "MAIL_S3_PREFIX_PROCESSED";
    public final static String WAR_FILE_ENV = "WAR_FILE";
    public final static String S3_ENABLED_ENV = "S3_ENABLED";
    public final static String S3_ACCESS_KEY_ENV = "S3_ACCESS_KEY";
    public final static String S3_KEY_ID_ENV = "S3_KEY_ID";
    public final static String S3_BUCKET_ENV = "S3_BUCKET";
    public final static String VB_S3_PREFIX_JSON_ENV = "VB_S3_PREFIX_JSON";

    static Map<String, String> properties = new HashMap<>();


    static {


        String configDir = null;
        String propFile = null;


        Configuration c = null;


        try {


            configDir = System.getProperty(CONFIG_DIR);
            if (configDir == null || configDir.trim().length() == 0)
                configDir = "conf";

            propFile = CONFIG_FILE;

            propFile = configDir + "/" + propFile;

            logger.info("Using bot.properties file location: {}", propFile);

            c = new PropertiesConfiguration(propFile);


        } catch (ConfigurationException e) {

            logger.error("Configuration Init Exception: ", e);
            c = null;

        }

        if(c!=null) {
            Config = c;
        }else{
            Config = new PropertiesConfiguration();
        }


        properties.put(MCP_NOTIFICATION_USER, MCP_NOTIFICATION_USER_ENV);
        properties.put(MCP_NOTIFICATION_PASSWORD, MCP_NOTIFICATION_PASSWORD_ENV);
        properties.put(KEYSTORE_PASSWORD, KEYSTORE_PASSWORD_ENV);
        properties.put(TRUSTSTORE_PASSWORD, TRUSTSTORE_PASSWORD_ENV);
        properties.put(SESSIONAUTH_URL, SESSIONAUTH_URL_ENV);
        properties.put(KEYAUTH_URL, KEYAUTH_URL_ENV);
        properties.put(SYMPHONY_POD, SYMPHONY_POD_ENV);
        properties.put(SYMPHONY_AGENT, SYMPHONY_AGENT_ENV);
        properties.put(CERTS_DIR, CERTS_DIR_ENV);
        properties.put(TRUSTSTORE_FILE, TRUSTSTORE_FILE_ENV);
        properties.put(BOT_USER, BOT_USER_ENV);
        properties.put(BOT_DOMAIN, BOT_DOMAIN_ENV);
        properties.put(FILES_JSON, FILES_JSON_ENV);
        properties.put(ESCO_USER_LIST, ESCO_USER_LIST_ENV);
        properties.put(ESCO_USER_VOTE, ESCO_USER_VOTE_ENV);
        properties.put(API_USER, API_USER_ENV);
        properties.put(API_PASSWORD, API_PASSWORD_ENV);
        properties.put(MAIL_SMTP_HOST, MAIL_SMTP_HOST_ENV);
        properties.put(MAIL_SMTP_USER, MAIL_SMTP_USER_ENV);
        properties.put(MAIL_SMTP_PASSWORD, MAIL_SMTP_PASSWORD_ENV);
        properties.put(MAIL_VB_FROM, MAIL_VB_FROM_ENV);
        properties.put(MAIL_SEND_EMAIL, MAIL_SEND_EMAIL_ENV);
        properties.put(MAIL_SMTP_AUTH, MAIL_SMTP_AUTH_ENV);
        properties.put(MAIL_SMTP_PORT, MAIL_SMTP_PORT_ENV);
        properties.put(MAIL_SMTP_DEBUG, MAIL_SMTP_DEBUG_ENV);
        properties.put(MAIL_DISTRO_EMAIL, MAIL_DISTRO_EMAIL_ENV);
        properties.put(WAR_FILE, WAR_FILE_ENV);
        properties.put(S3_ACCESS_KEY, S3_ACCESS_KEY_ENV);
        properties.put(S3_KEY_ID, S3_KEY_ID_ENV);
        properties.put(S3_ENABLED, S3_ENABLED_ENV);
        properties.put(S3_BUCKET, S3_BUCKET_ENV);
        properties.put(MAIL_S3_PREFIX_INCOMING, MAIL_S3_PREFIX_INCOMING_ENV);
        properties.put(MAIL_S3_PREFIX_PROCESSED, MAIL_S3_PREFIX_PROCESSED_ENV);
        properties.put(VB_S3_PREFIX_JSON, VB_S3_PREFIX_JSON_ENV);

        for (Map.Entry<String, String> entry : properties.entrySet()) {

            if (System.getProperty(entry.getKey()) == null) {

                if (System.getenv(entry.getValue()) != null) {
                    System.setProperty(entry.getKey(), System.getenv(entry.getValue()));
                } else {

                    if(Config.getString(entry.getKey()) == null)
                        Config.setProperty(entry.getKey(),"");

                    System.setProperty(entry.getKey(), Config.getString(entry.getKey()));
                }

            }


        }


    }


    public static Configuration getConfig() {
        return Config;
    }


}
