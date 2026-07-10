import java.time.Year

plugins {
    java
    signing
    `maven-publish`
    alias(libs.plugins.indraLicenserSpotless)
}

val javaVersion = 25

group = "net.hypejet"
version = System.getenv("GITHUB_REF_NAME") ?: "1.0-SNAPSHOT"
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

    repositories.maven {
        name = "mavenCentral"
        url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

        credentials {
            username = project.findProperty("mavenCentralUsername") as String?
            password = project.findProperty("mavenCentralPassword") as String?
        }
    }
}

signing {
    useInMemoryPgpKeys(
        project.findProperty("signingKey") as String?,
        project.findProperty("signingPassword") as String?
    )

    sign(publishing.publications["maven"])
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    property("YEAR", Year.now().value.toString())
}