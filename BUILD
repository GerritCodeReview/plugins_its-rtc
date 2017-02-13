load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "its-rtc",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Gerrit-PluginName: its-rtc",
        "Gerrit-Module: com.googlesource.gerrit.plugins.its.rtc.RTCModule",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.its.rtc.InitRTC",
        "Gerrit-ReloadMode: reload",
        "Implementation-Title: Plugin its-rtc",
        "Implementation-URL: http://www.gerritforge.com",
    ],
    deps = [
        "//plugins/its-base",
        "@commons_logging//jar",
    ],
    provided_deps = [
        "@commons-io//jar",
        "@gson//jar",
        "@commons_codec//jar",
        "@httpclient//jar",
        "httpcore//jar",
    ],
)

junit_tests(
    name = "its_rtc_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["its-bugzilla"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":its-rtc__plugin",
        "//plugins/its-base:its-base",
        "@mockito//jar",
    ],
)
