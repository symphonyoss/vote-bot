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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.vb.config.BotConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Handles all AWS S3 communications
 * <p>
 * This requires authentication access keys to access buckets for vote-bot
 *
 * @author Frank Tarsillo 3/26/17.
 */
public class AwsS3Client {

    private AmazonS3 s3Client;
    private Logger logger = LoggerFactory.getLogger(AwsS3Client.class);

    public AwsS3Client() {

        AWSCredentials credentials = new BasicAWSCredentials(System.getProperty(BotConfig.S3_KEY_ID), System.getProperty(BotConfig.S3_ACCESS_KEY));
        s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).build();


    }

    public static void main(String[] args) {

//        AwsS3Client awsClient = new AwsS3Client();
//
//
//        List<S3ObjectSummary> objectSummaries = awsClient.getAllObjects("symphony-esco", "email/incoming");
//
//        for (S3ObjectSummary objectSummary :
//                objectSummaries) {
//
//
//            System.out.println(" - " + objectSummary.getKey() + "  " +
//                    "(size = " + objectSummary.getSize() +
//                    " lastmodified = " + objectSummary.getLastModified() +
//                    " storage class = " + objectSummary.getStorageClass() +
//                    ")");
//
//            try {
//                awsClient.displayTextInputStream(awsClient.getObject(objectSummary));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }


    /**
     * Provide a list of objects from a given bucket w/prefix (folder).
     *
     * @param bucketName S3 bucket name
     * @param prefix     S3 folder within the bucket
     * @return List of {@link S3ObjectSummary} sorted by date
     */
    public List<S3ObjectSummary> getAllObjects(String bucketName, String prefix) {

        try {
            logger.debug("Listing S3 objects for s3://{}/{}", bucketName, prefix);
            final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix);
            ListObjectsV2Result result;
            List<S3ObjectSummary> allObjects = new ArrayList<>();

            do {
                result = s3Client.listObjectsV2(req);

                allObjects.addAll(result.getObjectSummaries());

                req.setContinuationToken(result.getNextContinuationToken());
            } while (result.isTruncated());


            allObjects.sort(Comparator.comparing(S3ObjectSummary::getLastModified));

            return allObjects;
        } catch (AmazonServiceException ase) {
            logger.error("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            logger.error("Error Message:    " + ase.getMessage());
            logger.error("HTTP Status Code: " + ase.getStatusCode());
            logger.error("AWS Error Code:   " + ase.getErrorCode());
            logger.error("Error Type:       " + ase.getErrorType());
            logger.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            logger.error("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            logger.error("Error Message: " + ace.getMessage());
        }

        return null;
    }


    public InputStream getObject(S3ObjectSummary objectSummary) {

        S3Object object=null;

        try {
            logger.info("Retrieving object inputstream for s3://{}/{}", objectSummary.getBucketName(), objectSummary.getKey());
            object = s3Client.getObject(
                    new GetObjectRequest(objectSummary.getBucketName(), objectSummary.getKey()));




        } catch (AmazonServiceException ase) {
            logger.error("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            logger.error("Error Message:    " + ase.getMessage());
            logger.error("HTTP Status Code: " + ase.getStatusCode());
            logger.error("AWS Error Code:   " + ase.getErrorCode());
            logger.error("Error Type:       " + ase.getErrorType());
            logger.error("Request ID:       " + ase.getRequestId());

        } catch (AmazonClientException ace) {
            logger.error("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            logger.error("Error Message: " + ace.getMessage());
        }
        return object == null ? null : object.getObjectContent();
    }

    public void putObject(String destBucket, String key, InputStream inputStream, ObjectMetadata metaData) {

        try {
            logger.info("Put object for s3://{}/{}", destBucket, key);
            byte[] bytes = IOUtils.toByteArray(inputStream);

            if(metaData==null)
                metaData = new ObjectMetadata();

            metaData.setContentLength(bytes.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            s3Client.putObject(new PutObjectRequest(destBucket, key, byteArrayInputStream, metaData));


        } catch (AmazonServiceException ase) {
            logger.error("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            logger.error("Error Message:    " + ase.getMessage());
            logger.error("HTTP Status Code: " + ase.getStatusCode());
            logger.error("AWS Error Code:   " + ase.getErrorCode());
            logger.error("Error Type:       " + ase.getErrorType());
            logger.error("Request ID:       " + ase.getRequestId());

        } catch (AmazonClientException ace) {
            logger.error("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            logger.error("Error Message: " + ace.getMessage());
        }catch (IOException e) {
            logger.error("Obtaining length", e);
        }

    }

    public void moveObject(S3ObjectSummary objectSummary, String destBucket, String destKey) {
        try {
            // Copying object
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(
                    objectSummary.getBucketName(), objectSummary.getKey(), destBucket, destKey);

            s3Client.copyObject(copyObjRequest);

            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(objectSummary.getBucketName(), objectSummary.getKey());

            s3Client.deleteObject(deleteObjectRequest);
        } catch (AmazonServiceException ase) {
            logger.error("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            logger.error("Error Message:    " + ase.getMessage());
            logger.error("HTTP Status Code: " + ase.getStatusCode());
            logger.error("AWS Error Code:   " + ase.getErrorCode());
            logger.error("Error Type:       " + ase.getErrorType());
            logger.error("Request ID:       " + ase.getRequestId());

        } catch (AmazonClientException ace) {
            logger.error("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            logger.error("Error Message: " + ace.getMessage());
        }
    }


    private void displayTextInputStream(InputStream input)
            throws IOException {

        if (input == null) {
            logger.error("InputStream was null..");
            return;
        }
        // Read one text line at a time and display.
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            logger.info("    {}", line);
        }
        logger.info("");
    }
}
