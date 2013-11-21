Plugin @PLUGIN@
===============

This plugin allows to associate IBM Rational Team Concert (RTC) issues to Git commits / Gerrit
Changes using the Gerrit ChangeListener and CommitValidator interfaces.

Main integration points provided are:

1. Commit validation (synchronous). It is the ability to introduce an additional validation
step to the Git commits pushed to Gerrit either directly to the target branch or submitted
for Code Review.
2. Commit association and workflow (asynchronous). It is the bi-directional integration between
the RTC issue and its corresponding Git commit / Gerrit Change. Additionally the workflow of
the RTC issue and Gerrit Review can be linked together using an external actions mapping file.
3. Configuration of comment links (init). It is an additional step included in the Gerrit init
configuration that allows to configure the RTC issue syntax and formatting all the references
rendered in Gerrit as hyperlinks to the corresponding issue target URL in RTC.

Comment links
----------------

Git commits are associated to RTC issues reusing the existing Gerrit
[commitLink configuration][1] to extract the issue ID from commit comments.

[1]: ../../../Documentation/config-gerrit.html#__a_id_commentlink_a_section_commentlink

Additionally you need to specify the enforcement policy for git commits
with regards to issue-tracker associations; the following values are supported:

`MANDATORY`
:	One or more issue-ids are required in the git commit message, otherwise
	the git push will be rejected. 
	NOTE: triggers a *synchronous API call* to RTC for the issue-id lookup, push
	is blocked until the operation is completed.

`SUGGESTED`
:	Whenever git commit message does not contain one or more issue-ids,
	a warning message is displayed as a suggestion on the client.
	NOTE: triggers a *synchronous API call* to RTC for the issue-id lookup, push
	is blocked until the operation is completed.

`OPTIONAL`
:	 Issues-ids are liked when found on git commit message, no warning are
	 displayed otherwise.

**Example:**

    [commentLink "RTC"]
    match = RTC#([0-9]*)
    html = "<a href=\"https://rtc.gerritforge.com:9443/ccm/browse/$1\">$1</a>"
    association = OPTIONAL

Once a Git commit with a comment link is detected, the RTC issue ID
is extracted and a new comment added to the issue, pointing back to
the original Git commit.

RTC connectivity
-----------------

In order for Gerrit to connect to RTC REST-API, url and credentials
are required in your gerrit.config / secure.config under the [rtc] section.

**Example:**

    [rtc]
    url=https://rtc.gerritforge.com:9443/ccm
    username=rtcuser
    passsword=rtcpass

RTC credentials and connectivity details are asked and verified during the Gerrit init.

HTTP/S and network settings
---------------------------

There are no additional settings required for a default connectivity from Gerrit
to RTC and the default JVM settings are automatically taken for opening outbound 
connections.

However connectivity to RTC could be highly customised for defining the protocol
security level, pooling and network settings. This allows the administrator
to have full control of the output pipe to RTC and the propagation of the Change events
to the associated issues in a high-loaded production environment.

All settings are defined in gerrit.config under the same [rtc] section.
See below the list of the most important parameters and their associated meaning.

`sslVerify`
:	`[TRUE|FALSE]`. When using HTTP/S to connect to RTC (the most typical scenario)
	allows to enforce (recommended) or disable (**ONLY FOR TEST ENVIRONMENTS**) the 
	X.509 Certificates validation during SSL handshake. If unsure say TRUE.
	
`httpSocketTimeout`
:	`<number>` Defines the socket timeout in milliseconds,
    which is the timeout for waiting for data  or, put differently,
    a maximum period inactivity between two consecutive data packets).
    A timeout value of zero is interpreted as an infinite timeout.

`httpSocketBufferSize`
:	`<number>` Determines the size of the internal socket buffer used to buffer data
    while receiving / transmitting HTTP messages.

`httpSocketReuseaddr`
:	`[TRUE|FALSE]` Defines whether the socket can be bound even though a previous connection is
    still in a timeout state.

`httpConnectionTimeout`
:	`<number>` Determines the timeout in milliseconds until a connection is established.
    A timeout value of zero is interpreted as an infinite timeout.

`httpConnectionStalecheck`
:	`[TRUE|FALSE]` Determines whether stale connection check is to be used. The stale
    connection check can cause up to 30 millisecond overhead per request and
    should be used only when appropriate. For performance critical
    operations this check should be disabled.

`httpSocketKeepalive`
:	`[TRUE|FALSE]` Defines whether or not TCP is to send automatically a keepalive probe to the peer
	after an interval of inactivity (no data exchanged in either direction) between this
	host and the peer. The purpose of this option is to detect if the peer host crashes.
	
`httpConnManagerTimeout`
:	`<number>` Defines the timeout in milliseconds used when retrieving a free connection from the pool
	of outbound HTTP connections allocated.
	
`httpConnManagerMaxTotal`
:	`<number>` Defines the maximum number of outbound HTTP connections in total.
    This limit is interpreted by client connection managers and applies to individual manager instances.

**NOTE**: The full list of all available HTTP network connectivity parameters can be found under
the [Apache Commons HTTP Client 4.2.x documentation](http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/index.html?org/apache/http/client/params/ClientPNames.html). Gerrit parameters names are the [CamelCase](http://en.wikipedia.org/wiki/Camelcase) version of the string
values of the Apache HTTP Client ones.


Gerrit init integration
-----------------------

RTC plugin is integrated as a Gerrit init step in order to simplify and guide
through the configuration of RTC integration and connectivity check, avoiding
bogus settings to prevent Gerrit plugin to start correctly.

**Gerrit init example:**

    *** IBM Rational Team Concert connectivity
	*** 

	RTC CCM URL (empty to skip)    : https://rtc.gerritforge.com:9443/ccm
	RTC username                   []: gerrit
	Change luca's password         [y/N]? y
	luca's password                : ******
	              confirm password : ******
	Verify SSL Certificates        [TRUE/?]: false
	Test connectivity to https://rtc.gerritforge.com:9443/ccm [N/?]: y
	Checking IBM Rational Team Concert connectivity ... [OK]

	*** Rational Team Concert issue-tracking association
	*** 

	RTC issue-Id regex             [RTC#([0-9]+)]: 
	RTC-Id enforced in commit message [OPTIONAL/?]: suggested
	
Issues workflow automation
--------------------------

RTC plugin is able to automate status transition on the issues based on
code-review actions performed on Gerrit; actions are performed on RTC using
the username/password provided during Gerrit init.
Transition automation is driven by `$GERRIT_SITE/issue-state-transition.config` file.

Syntax of the status transition configuration file is the following:

    [action "<issue-status-action>"]
    change=<change-action>
    verified=<verified-value>
    code-review=<code-review-value>

`<issue-status-action>`
:	Action to perform on RTC issue when all the condition in the stanza are met.

`<change-action>`
:	Action performed on Gerrit change-id, possible values are:
	`created, commented, merged, abandoned, restored`

`<verified-value>`
:	Verified flag added on Gerrit with values from -1 to +1

`<code-review-value>`
:	Code-Review flag added on Gerrit with values from -2 to +2

Note: multiple conditions in the action stanza are optional but at least one must be present.

Example:

    [action "Start Progress"]
    change=created

    [action "Resolve Issue"]
    verified=+1
    code-review=+2

    [action "Close Issue"]
    change=merged

    [action "Stop Progress"]
    change=abandoned

The above example defines four status transition on RTC, based on the following conditions:

* Whenever a new Change-set is created on Gerrit, start progress on the RTC issue
* Whenever a change is verified and reviewed with +2, transition the RTC issue to resolved
* Whenever a change is merged to branch, mark the RTC transition the RTC issue to closed
* Whenever a change is abandoned, stop the progress on RTC issue
