The bot provides vote capabilities on proposals amongst a defined list of users over a relay chat.

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

Owner: Frank Tarsillo


