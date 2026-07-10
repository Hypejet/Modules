import java.time.Year

plugins {
    java
    `maven-publish`
    alias(libs.plugins.indraLicenserSpotless)
}

val javaVersion = 25

group = "net.hypejet"
version = "1.0-SNAPSHOT"

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
        }
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    property("YEAR", Year.now().value.toString())
}