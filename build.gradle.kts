import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { // apply false doesn't apply these for root project. This is only for managing version numbers.
    kotlin("jvm") apply false
    id("org.springframework.boot") version "2.4.0-SNAPSHOT" apply false
    id("io.gitlab.arturbosch.detekt") version "1.7.4"
    id("com.adarshr.test-logger") version "2.0.0"
}

subprojects {
    apply { // Above plugins are applied here.
        plugin("org.jetbrains.kotlin.jvm")
    }

    repositories {
        jcenter()
        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
        maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
        maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
    }

    val implementation by configurations
    val arrowVersion = "0.10.5"

    dependencies {
        implementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR3")) // This is same as importing `mavenBom` using `io.spring.dependency-management` plugin

        implementation("org.springframework.fu:spring-fu-kofu:0.4.0-SNAPSHOT")
        implementation("io.arrow-kt:arrow-core:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx-reactor:$arrowVersion")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_14.toString()
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
            excludeEngines("junit-vintage")
        }
    }
}

// Dependencies except for "validation-templates" project
configure(subprojects.filter { it.name != "validation-templates" }) {
    val implementation by configurations
    val testImplementation by configurations
    dependencies {
        implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)) // For spring boot bom
        implementation(project(":validation-templates"))
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        testImplementation("org.springframework.boot:spring-boot-starter-webflux")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}

testlogger {
    setTheme("mocha")
    showExceptions=true
    showStackTraces=true
    showFullStackTraces=false
    showCauses=true
    slowThreshold=2000
    showSummary=true
    showSimpleNames=true
    showPassed=true
    showSkipped=true
    showFailed=true
    showStandardStreams=true
    showPassedStandardStreams=true
    showSkippedStandardStreams=true
    showFailedStandardStreams=true
}
