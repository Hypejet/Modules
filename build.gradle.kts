import java.time.Year

plugins {
    java
    signing
    `maven-publish`
    alias(libs.plugins.indraLicenserSpotless)
}

val javaVersion = 25

group = "net.hypejet"
version = "1.0-SNAPSHOT"
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
    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
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
    }
}

signing {
    sign(publishing.publications["maven"])
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    property("YEAR", Year.now().value.toString())
}