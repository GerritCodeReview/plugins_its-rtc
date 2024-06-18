load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
  maven_jar(
    name = 'commons-logging',
    artifact = 'commons-logging:commons-logging:1.2',
    sha1 = '4bfc12adfe4842bf07b657f0369c4cb522955686',
  )

  maven_jar(
      name = "jaxb-api",
      artifact = "javax.xml.bind:jaxb-api:2.3.1",
  )
