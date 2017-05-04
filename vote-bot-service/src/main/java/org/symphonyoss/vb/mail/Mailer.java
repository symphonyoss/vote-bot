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

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

import static org.symphonyoss.vb.config.BotConfig.Config;

/**
 * Created by Frank on 1/18/2016.
 */
public class Mailer {
    final int PORT = 465;
    Transport transport;
    Session mailSession;
    MimeMessage message;
    private Logger logger = LoggerFactory.getLogger(Mailer.class);


    public Mailer() {

        try {

            System.setProperty("line.separator", "\r\n");

            // Create a properties file containing the host address of
            // your SMTP server
            Properties mailProps = new Properties();
            //  mailProps.put("mail.smtp.host", "smtp.myispserver.net");
            //   mailProps.put("mail.smtp.host", "k2smtpout.secureserver.net");
            mailProps.put("mail.transport.protocol", "smtps");
            mailProps.put("mail.smtp.host", System.getProperty(BotConfig.MAIL_SMTP_HOST, "localhost"));
            mailProps.put("mail.smtp.user", System.getProperty(BotConfig.MAIL_SMTP_USER,"mail.smtp.user"));
            mailProps.put("mail.smtp.auth", Config.getString("mail.smtp.auth"));
            mailProps.put("mail.smtp.starttls.enable", "true");
            mailProps.put("mail.smtp.starttls.required", "true");
            mailProps.put("mail.smtps.ssl.checkserveridentity", "false");
            mailProps.put("mail.smtps.ssl.trust", "*");
            mailProps.put("mail.smtp.port", PORT);
            mailProps.put("mail.debug", "true");


            // Create a Session object to represent a mail session with the specified properties.
            mailSession = Session.getDefaultInstance(mailProps, new SMTPAuthenticator());
            message = new MimeMessage(mailSession);
            transport = mailSession.getTransport();

            transport.connect(System.getProperty(BotConfig.MAIL_SMTP_HOST, "localhost"), System.getProperty(BotConfig.MAIL_SMTP_USER,"mail.smtp.user"), System.getProperty(BotConfig.MAIL_SMTP_PASSWORD,"mail.smtp.password"));


        } catch (Exception ee) {
            logger.error("Could not send reg confirm email.." + ee);

        }


    }


    public Mailer to(String to) {

        try {
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Mailer from(String from) {

        // Set the From and the Recipient
        try {
            message.setFrom(new InternetAddress(from));
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return this;
    }

    public Mailer subject(String subject) {
        try {
            message.setSubject(subject);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Mailer body(String body) {
        try {
            message.setContent(body, "text/plain");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return this;
    }

    public static void send(Consumer<Mailer> block) {
        Mailer mailer = new Mailer();

        block.accept(mailer);


        mailer.saveAndSend();


    }


    public static Message getMessage(InputStream mail){
        try {

            System.setProperty("line.separator", "\r\n");

            // Create a properties file containing the host address of
            // your SMTP server
            Properties mailProps = new Properties();
            mailProps.put("mail.transport.protocol", "smtps");
            mailProps.put("mail.smtp.host", System.getProperty(BotConfig.MAIL_SMTP_HOST, "localhost"));
            mailProps.put("mail.smtp.user", System.getProperty(BotConfig.MAIL_SMTP_USER,"mail.smtp.user"));
            mailProps.put("mail.smtp.auth", System.getProperty(BotConfig.MAIL_SMTP_AUTH, "mail.smtp.auth"));
            mailProps.put("mail.smtp.starttls.enable", "true");
            mailProps.put("mail.smtp.starttls.required", "true");
            mailProps.put("mail.smtps.ssl.checkserveridentity", "false");
            mailProps.put("mail.smtps.ssl.trust", "*");
            mailProps.put("mail.smtp.port", Config.getInt(BotConfig.MAIL_SMTP_PORT));
            mailProps.put("mail.debug", Config.getString(BotConfig.MAIL_SMTP_DEBUG));


            // Create a Session object to represent a mail session with the specified properties.
            Session mailSession = Session.getDefaultInstance(mailProps, new SMTPAuthenticator());


            return  new MimeMessage(mailSession, mail);


        } catch (Exception ee) {
            System.out.println("Error processing message" + ee);

        }

        return null;

    }

    private void saveAndSend() {
        try {


            // Save all the changes you have made to the message
            message.saveChanges();

            transport.sendMessage(message, message.getAllRecipients());


            transport.close();


        } catch (Exception ee) {
            logger.error("Could not send reg confirm email.." + ee);

        }

    }



    private static class SMTPAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(System.getProperty(BotConfig.MAIL_SMTP_USER), System.getProperty(BotConfig.MAIL_SMTP_PASSWORD));
        }
    }


}
