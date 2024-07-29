import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

group = "com.twingineer"
version = "0.1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.terpal) // for test compilation
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
                implementation(libs.kotlinx.html.common)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.terpal.runtime)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.slf4j.simple)
            }
        }
    }
}

tasks.withType(KotlinCompilationTask::class.java) {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    kotlin.compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}