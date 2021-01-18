import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { // apply false doesn't apply these for root project. This is only for managing version numbers.
    kotlin("jvm") apply false
    id("org.springframework.boot") version "2.4.0-SNAPSHOT" apply false
    id("io.gitlab.arturbosch.detekt") version "1.14.1"
    id("com.adarshr.test-logger") version "2.1.1"
}

subprojects {
    group = "io.overfullstack"
    version = "0.0.1-SNAPSHOT"

    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.adarshr.test-logger")
        plugin("io.gitlab.arturbosch.detekt")
    }

    repositories {
        jcenter()
        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
        maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
        maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
    }

    val arrowVersion = "0.10.5"

    dependencies {
        val implementation by configurations

        // This is same as importing `mavenBom` using `io.spring.dependency-management` plugin
        implementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR3"))

        implementation("org.springframework.fu:spring-fu-kofu:0.4.0-SNAPSHOT")
        implementation("io.arrow-kt:arrow-core:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx-reactor:$arrowVersion")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_15.toString()
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            excludeEngines("junit-vintage")
        }
    }

    testlogger {
        setTheme("mocha")
        showExceptions = true
        showStackTraces = true
        showFullStackTraces = false
        showCauses = true
        slowThreshold = 2000
        showSummary = true
        showSimpleNames = true
        showPassed = true
        showSkipped = true
        showFailed = true
        showStandardStreams = true
        showPassedStandardStreams = true
        showSkippedStandardStreams = true
        showFailedStandardStreams = true
    }

    detekt {
        baseline = file("${rootProject.projectDir}/config/baseline.xml")
        config = files("config/detekt/detekt.yml")
        buildUponDefaultConfig = true
    }
}

// Dependencies except for "validation-templates" project
configure(subprojects.filter { it.name != "validation-templates" }) {
    dependencies {
        val implementation by configurations
        val testImplementation by configurations

        implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)) // For spring boot bom
        implementation(project(":validation-templates"))
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        testImplementation("org.springframework.boot:spring-boot-starter-webflux")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}
