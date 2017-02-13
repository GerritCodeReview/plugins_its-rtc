load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
  maven_jar(
    name = 'commons-logging',
    artifact = 'commons-logging:commons-logging:1.2',
    sha1 = '4bfc12adfe4842bf07b657f0369c4cb522955686',
  )

  maven_jar(
    name = 'mockito',
    artifact = 'org.mockito:mockito-all:1.9.5',
    sha1 = '79a8984096fc6591c1e3690e07d41be506356fa5',
  )
