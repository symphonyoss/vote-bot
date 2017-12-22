[![Vote Bot Prod Log Watcher](https://hv0dbm9dsd.execute-api.us-east-1.amazonaws.com/Prod/badge?oc_bot_name=votebot-prod&oc_project_name=ssf-prod)](https://foundation.symphony.com)
[![Vote Bot Dev Log Watcher](https://hv0dbm9dsd.execute-api.us-east-1.amazonaws.com/Prod/badge?oc_bot_name=votebot-dev&oc_project_name=ssf-dev)](https://foundation-dev.symphony.com)

The bot provides vote capabilities on proposals amongst a defined list of users over a relay chat.

# Build
Type `mvn package`.

When the build completes, the ZIP file containing all bot logic can be found at `vote-bot-service/target/vote-bot-service-0.9.1-SNAPSHOT-appassembler.zip`.

## Openshift

In order to run these commands you need to have access to the Openshift YAML definitions, which are managed by Foundation Staff.

### Deploy to Openshift

```
. ./env-dev.sh
curl -s https://raw.githubusercontent.com/symphonyoss/contrib-toolbox/master/scripts/oc-deploy.sh | bash
```

Make sure the following vars are included in `env-dev.sh`.
```
export OC_TOKEN=<your oc token>
export OC_PROJECT=botfarm
export OC_BINARY_FOLDER="vote-bot-service/target/oc"
# Choose between votebot-dev or votebot-prod
export OC_BUILD_CONFIG_NAME="votebot-dev"
export OC_ENDPOINT="https://api.pro-us-east-1.openshift.com"
export OC_PROJECT_NAME="ssf-dev"
export ESCO_USER_VOTE=""
export ESCO_USER_LIST=""
export OC_TEMPLATE_PROCESS_ARGS="-p ESCO_USER_LIST=$ESCO_USER_LIST -p ESCO_USER_VOTE=$ESCO_USER_VOTE"
```

When configuring the environment variables via Travis CI settings, make sure to escape ' ', '(' and ')' chars, adding a '\' before.

### Reset Openshift environment

To delete all existing Openshift resources, simply invoke `oc delete all -l app=votebot-dev` (secrets, which are managed by Foundation staff, will not be removed)

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
* Support for latest V4 MessageMLv2


### RATIONALE

Voting concepts are common in many social engagements.  Having a solution that can manage and record voting events is applicable to wide variety of use cases on a social network.   

### CORE DEVELOPERS

Owner: Frank Tarsillo and Friends


