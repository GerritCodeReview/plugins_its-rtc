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
    manifest_entries = [
        "Gerrit-PluginName: its-rtc",
        "Gerrit-Module: com.googlesource.gerrit.plugins.its.rtc.RTCModule",
        "Gerrit-InitStep: com.googlesource.gerrit.plugins.its.rtc.InitRTC",
        "Gerrit-ReloadMode: reload",
        "Implementation-Title: Plugin its-rtc",
        "Implementation-URL: http://www.gerritforge.com",
    ],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "//plugins/its-base",
        "@commons_codec//jar:neverlink",
        "@commons_io//jar",
        "@commons_logging//jar",
    ],
)

junit_tests(
    name = "its_rtc_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["its-rtc"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":its-rtc__plugin",
        "//plugins/its-base:its-base",
        "@mockito//jar",
    ],
)
