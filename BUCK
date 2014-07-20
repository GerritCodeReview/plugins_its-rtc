include_defs('//bucklets/gerrit_plugin.bucklet')

ITS_BASE = '//lib:its-base' if __standalone_mode__ \
  else '//plugins/its-base:its-base__plugin'

gerrit_plugin(
  name = 'its-rtc',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: its-rtc',
    'Gerrit-Module: com.googlesource.gerrit.plugins.hooks.rtc.RTCModule',
    'Gerrit-InitStep: com.googlesource.gerrit.plugins.hooks.rtc.InitRTC',
    'Gerrit-ReloadMode: reload',
    'Implementation-Title: Plugin its-rtc',
    'Implementation-URL: http://www.gerritforge.com',
    'Implementation-Vendor: GerritForge LLP',
  ],
  deps = [
    ITS_BASE,
    align_path('its-rtc', '//lib:commons-logging'),
  ],
  provided_deps = [
    '//lib:gson',
    '//lib/commons:codec',
    '//lib/commons:httpclient',
    '//lib/commons:httpcore',
    '//lib/commons:io',
  ],
)

java_library(
  name = 'classpath',
  deps = [':its-rtc__plugin'],
)

java_test(
  name = 'its-rtc_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['its-rtc'],
  source_under_test = [':its-rtc__plugin'],
  deps = GERRIT_PLUGIN_API + [
    ':its-rtc__plugin',
    '//lib:junit',
    align_path('its-rtc', '//lib:mockito'),
  ],
)
