import java.time.Year

plugins {
    java
    alias(libs.plugins.indraLicenserSpotless)
    alias(libs.plugins.mavenPublish)
}

val javaVersion = 25

group = "net.hypejet"
version = releaseTag() ?: "1.0.3-SNAPSHOT"
description = "A Java library making modular programming easier"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jspecify)
    implementation(libs.jetbrainsAnnotations)
    implementation(libs.slf4j)
    testImplementation(libs.junitJupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withJavadocJar()
    withSourcesJar()
}

tasks {
    compileJava {
        options.release = javaVersion
    }

    javadoc {
        val docletOptions = options as StandardJavadocDocletOptions
        docletOptions.addBooleanOption("html5", true)
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])

        pom {
            artifactId = project.name.lowercase()

            name = project.name
            description = project.description
            url = "https://github.com/Hypejet/Modules"

            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }

            developers {
                developer {
                    id = "Codestech1"
                    name = "Codestech"
                    email = "codestech@hypejet.net"
                }
            }

            scm {
                connection = "scm:git:git://github.com/Hypejet/Modules.git"
                developerConnection = "scm:git:ssh://github.com:Hypejet/Modules.git"
                url = "https://github.com/Hypejet/Modules"
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    property("YEAR", Year.now().value.toString())
}

private fun releaseTag(): String? {
    val ref = System.getenv("GITHUB_REF") ?: return null
    val tagRefPrefix = "refs/tags/"

    if (!ref.startsWith(tagRefPrefix))
        return null

    val tag = ref.removePrefix(tagRefPrefix)
    if (tag.isEmpty() || !tag.startsWith("v")) return null
    return tag.removePrefix("v").ifEmpty { null }
}