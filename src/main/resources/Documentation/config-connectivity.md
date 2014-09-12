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

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
