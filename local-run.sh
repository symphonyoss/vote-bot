#!/bin/bash

. ./env-prod.sh

mvn clean package

cd vote-bot-service/target
rm -rf vote-bot-service-0.9.1-SNAPSHOT
unzip vote-bot-service-0.9.1-SNAPSHOT-appassembler.zip
cp -rf ../../certs vote-bot-service-0.9.1-SNAPSHOT
cd vote-bot-service-0.9.1-SNAPSHOT

./bin/VoteBot
