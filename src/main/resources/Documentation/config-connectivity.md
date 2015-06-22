RTC connectivity
================

In order for @PLUGIN@ to connect to RTC instance, url and credentials
are required in your site's `etc/gerrit.config` or `etc/secure.config`
under the `@PLUGIN@` section.

Example:

```
[@PLUGIN@]
  url=http://my.rtc.instance.example.org
  username=USERNAME_TO_CONNECT_TO_RTC
  password=PASSWORD_FOR_ABOVE_USERNAME
```

HTTP/S and network settings
---------------------------

There are no additional settings required for a default connectivity from Gerrit
to RTC and the default JVM settings are automatically taken for opening outbound
connections.

However connectivity to RTC could be highly customised for defining the protocol
security level, pooling and network settings. This allows the administrator to
have full control of the output pipe to RTC and the propagation of the Change
events to the associated issues in a high-loaded production environment.

All settings are defined in gerrit.config under the same `[@PLUGIN@]` section.
See below the list of the most important parameters and their associated
meaning.

`sslVerify`
:   `true` or `false`. When using HTTP/S to connect to RTC (the most typical
    scenario) allows to enforce (recommended) or disable
    ( **ONLY FOR TEST ENVIRONMENTS** ) the X.509 Certificates validation during
    SSL handshake.  If unsure say `true`.

`httpSocketTimeout`
:	`<number>` Defines the socket timeout in milliseconds,
    which is the timeout for waiting for data  or, put differently,
    a maximum period inactivity between two consecutive data packets).
    A timeout value of zero is interpreted as an infinite timeout.

`httpSocketBufferSize`
:   `<number>` Determines the size of the internal socket buffer used to
    buffer data while receiving / transmitting HTTP messages.

`httpSocketReuseaddr`
:   `true` or `false`. Defines whether the socket can be bound even though a
    previous connection is still in a timeout state.

`httpConnectionTimeout`
:   `<number>` Determines the timeout in milliseconds until a connection is
    established. A timeout value of zero is interpreted as an infinite timeout.

`httpConnectionStalecheck`
:   `true` or `false`. Determines whether stale connection check is to be
    used. The stale connection check can cause up to 30 millisecond overhead per
    request and should be used only when appropriate. For performance critical
    operations this check should be disabled.

`httpSocketKeepalive`
:   `true` or `false`. Defines whether or not TCP is to send automatically
    a keepalive probe to the peer after an interval of inactivity (no data
    exchanged in either direction) between this host and the peer. The purpose
    of this option is to detect if the peer host crashes.

`httpConnManagerTimeout`
:   `<number>` Defines the timeout in milliseconds used when retrieving a free
    connection from the pool of outbound HTTP connections allocated.

`httpConnManagerMaxTotal`
:   `<number>` Defines the maximum number of outbound HTTP connections in total.
    This limit is interpreted by client connection managers and applies to
    individual manager instances.

**NOTE**: The full list of all available HTTP network connectivity parameters can be found under
the [Apache Commons HTTP Client 4.3.x documentation](http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/index.html?org/apache/http/client/params/ClientPNames.html). Gerrit parameters names are the [CamelCase](http://en.wikipedia.org/wiki/Camelcase) version of the string
values of the Apache HTTP Client ones.


[Back to @PLUGIN@ documentation index][index]

[index]: index.html
