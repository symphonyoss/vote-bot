The bot provides vote capabilities on proposals amongst a defined list of users over a relay chat.

# Build

Type `mvn package`.

When the build completes, the ZIP file containing all bot logic can be found at `vote-bot-service/target/vote-bot-service-0.9.0-SNAPSHOT-appassembler.zip`.

# Deploy to Openshift

First of all, select which environment/build to target by setting these environment variables:

```
export OC_PROJECT=botfarm
# Choose between votebot-dev and votebot-prod
export OC_DEPLOY=votebot-dev
```

1. [Install Openshift commandline](https://docs.openshift.org/latest/cli_reference/get_started_cli.html) tool `oc`
2. Authenticate against Openshift Online instance of the Symphony Software Foundation using `oc login https://api.starter-us-east-1.openshift.com --token=$OC_TOKEN` ; the `OC_TOKEN` can be requested to Foundation Staff
3. Use Openshift project using `oc project $OC_PROJECT`
4. Checkout this project, `cd` into the root folder and build the project with `mvn package`
5. Unzip the package being created using `unzip vote-bot-service/target/vote-bot-service-0.9.0-SNAPSHOT-appassembler.zip -d vote-bot-service/target/oc`
6. Deploy the archive created using `oc start-build $OC_DEPLOY --from-dir=vote-bot-service/target/oc/vote-bot-service-0.9.0-SNAPSHOT/ --wait=true -n $OC_PROJECT`

# Reset Openshift environment
In order to run these commands you need to have access to the Openshift YAML definitions, which are managed by Foundation Staff.

1. Delete all existing Openshift resources - `oc delete imagestream $OC_DEPLOY; oc delete buildconfig $OC_DEPLOY; oc delete dc $OC_DEPLOY; oc delete svc $OC_DEPLOY; oc delete route $OC_DEPLOY; oc delete secret $OC_DEPLOY.certs`
2. Create Openshift secrets - `oc process -f ./secrets/$OC_DEPLOY.yaml | oc create -f -`
3. Create other Openshift resources - `oc process -f ./builds/$OC_DEPLOY-build-template.yaml | oc create -f -`

### PROPOSAL

To create a bot that can support generic voting capabilities over the Symphony network.  The initial contribution is built to specifically support proposals over a relay chat, but it can be expanded to support wide variety of voting types.

The current contributed feature set of the solution includes:
* Single bot endpoint representing vote subsystem for a defined user base.
* Relay chat functionality which relays messages amongst a statically defined set of member users.  Relay design pattern is a hub-spoke model with identity display names prefixed in relayed messages. (from: vote-bot: Frank Tarsillo: Message)
* Static list of users can have two different roles.  Member) User has the ability to read and transmit general relay messages, view current voting proposals and receive email notifications.  Participant) User has all member rights, but can also vote on proposal
* Voting criteria can be defined.
* Command subsystem supporting 1:1 vote commands

		/new [`Short Desc`] [`Detailed Desc`] Create a new vote.
		/vote [VoteID] [yea/nay/abstain] Place a vote.
		/list optional:[active/VoteID/archive p#] List votes by criteria (default: active)
		/comment [VoteID] [comment] Add a comment to a registered vote
		[/cancel [VoteID] Cancel an active vote.

* Comment support for proposals
* Reminder notifications on all voting proposals to participants over IM and email.
* Local storage for recording of all proposals using basic JSON objects.  Support for S3
* Email notifications
* Archiving w/IM paging
* Proposal recovery from restarts
* Voting using emoticons (too many!)
* Auto-accept connection requests

Future work:
* Chat to email support for external users
* Different voting types supported (e.g. Election)
* Virtual voting bodies (through one bot)
* Vote-bot for chat rooms
* Encrypted Signing


### RATIONALE

Voting concepts are common in many social engagements.  Having a solution that can manage and record voting events is applicable to wide variety of use cases on a social network.   

### CORE DEVELOPERS

Owner: Frank Tarsillo and Friends


