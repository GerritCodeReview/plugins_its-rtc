gerrit_plugin(
  name = 'its-rtc',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: its-jira',
    'Gerrit-Module: com.googlesource.gerrit.plugins.hooks.rtc.RTCModule',
    'Gerrit-InitStep: com.googlesource.gerrit.plugins.hooks.rtc.InitRTC',
    'Gerrit-ReloadMode: reload',
    'Implementation-Title: Plugin its-rtc',
    'Implementation-URL: http://www.gerritforge.com',
    'Implementation-Vendor: GerritForge LLP',
  ],
  deps = [
    '//plugins/its-base:its-base__plugin',
    '//plugins/its-rtc/lib:commons-logging',
  ],
  provided_deps = [
    '//lib:gson',
    '//lib/commons:codec',
    '//lib/commons:httpclient',
    '//lib/commons:httpcore',
    '//lib/commons:io',
  ],
)

java_test(
  name = 'its-rtc_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['its-rtc'],
  source_under_test = [':its-rtc__plugin'],
  deps = [
    ':its-rtc__plugin',
    '//gerrit-plugin-api:lib',
    '//lib:junit',
    '//plugins/its-rtc/lib:mockito',
  ],
)
