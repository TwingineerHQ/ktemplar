import com.vanniktech.maven.publish.SonatypeHost

group = "com.twingineer"
version = "0.1.3"
description = "Fluent, safe templating in 100% Kotlin."

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.terpal) // for test compilation
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
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

                implementation(libs.jetbrains.annotations)
                implementation(libs.kotlin.logging)
                implementation(libs.kotlinx.html.common)
                implementation(libs.kotlinx.serialization.json)

                compileOnly(libs.terpal.runtime)
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
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        name.set("Ktemplar")
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
                url.set("https://twingineer.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/TwingineerHQ/ktemplar.git")
            developerConnection.set("scm:git:ssh://github.com:TwingineerHQ/ktemplar.git")
            url.set("https://github.com/TwingineerHQ/ktemplar/tree/main")
        }
    }

    signAllPublications()
}