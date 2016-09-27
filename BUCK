include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'its-rtc',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: its-rtc',
    'Gerrit-Module: com.googlesource.gerrit.plugins.its.rtc.RTCModule',
    'Gerrit-InitStep: com.googlesource.gerrit.plugins.its.rtc.InitRTC',
    'Gerrit-ReloadMode: reload',
    'Implementation-Title: Plugin its-rtc',
    'Implementation-URL: http://www.gerritforge.com',
    'Implementation-Vendor: GerritForge LLP',
  ],
  deps = [
    ':its-base_stripped',
    '//plugins/its-rtc/lib:commons-logging',
    '//plugins/its-rtc/lib:commons-io',
  ],
  provided_deps = [
    '//lib:gson',
    '//lib/commons:codec',
    '//lib/httpcomponents:httpclient',
    '//lib/httpcomponents:httpcore',
  ],
)

def strip_jar(
    name,
    src,
    excludes = [],
    visibility = [],
  ):
  name_zip = name + '.zip'
  genrule(
    name = name_zip,
    cmd = 'cp $SRCS $OUT && zip -qd $OUT ' + ' '.join(excludes),
    srcs = [ src ],
    out = name_zip,
    visibility = visibility,
  )
  prebuilt_jar(
    name = name,
    binary_jar = ':' + name_zip,
    visibility = visibility,
  )

strip_jar(
  name = 'its-base_stripped',
  src = '//plugins/its-base:its-base',
  excludes = [
    'Documentation/about.md',
    'Documentation/build.md',
    'Documentation/config-connectivity.md',
  ]
)

java_test(
  name = 'its-rtc_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['its-rtc'],
  deps = GERRIT_PLUGIN_API + GERRIT_TESTS + [
    ':its-rtc__plugin',
    '//lib:junit',
    '//plugins/its-rtc/lib:mockito',
  ],
)
