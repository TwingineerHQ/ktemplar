import org.jreleaser.model.Active

group = "com.twingineer"
version = "0.1.0-SNAPSHOT"
description = "Fluent, safe templating in 100% Kotlin."

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.terpal) // for test compilation
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()

    jvm {
        withJava()
    }
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useFirefox()
                }
            }
        }
        nodejs()

        useEsModules()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.kotlin.bom))

                implementation(libs.kotlin.logging)
                implementation(libs.terpal.runtime)
                implementation(libs.kotlinx.html.common)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.slf4j.simple)
            }
        }
    }
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar)

        pom {
            name.set("ktemplar")
            description.set(project.description)
            url.set("https://github.com/TwingineerHQ/ktemplar")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    name.set("Twingineer")
                    email.set("solutions@twingineer.com")
                    organization.set("Twingineer")
                    organizationUrl.set("https://twingineer.com")
                }
            }

            organization {
                name.set("Twingineer")
                url.set("https://twingineer.com")
            }

            scm {
                connection.set("scm:git:git@github.com:TwingineerHQ/ktemplar.git")
                developerConnection.set("scm:git:ssh:git@github.com:TwingineerHQ/ktemplar.git")
                url.set("https://github.com/TwingineerHQ/ktemplar/tree/main")
            }
        }
    }

    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

jreleaser {
    project {
        copyright.set("Twingineer")
    }
    signing {
        active.set(Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(Active.ALWAYS)
                    applyMavenCentralRules.set(true)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("build/staging-deploy")
                    snapshotSupported = true
                }
            }
        }
    }
}