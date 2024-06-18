load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
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
        "@commons-codec//jar:neverlink",
        "@commons-io//jar",
        "@commons-logging//jar",
        "@jaxb-api//jar",
    ],
)

junit_tests(
    name = "its_rtc_tests",
    testonly = 1,
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["its-rtc"],
    deps = ["its-rtc__plugin_test_deps"],
)

java_library(
    name = "its-rtc__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":its-rtc__plugin",
        "//plugins/its-base:its-base",
        "@mockito//jar",
    ],
)
