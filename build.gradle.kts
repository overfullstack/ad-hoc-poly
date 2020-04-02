import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.71" apply false
    id("io.spring.dependency-management") version "1.0.9.RELEASE" apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.spring.dependency-management") // This takes care of spring related version management
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
        maven("https://dl.bintray.com/arrow-kt/arrow-kt/")
        maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
    }

    val implementation by configurations
    val arrowVersion = "0.10.5"

    dependencies {
        implementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR3")) // This is same as mavenBom

        implementation(kotlin("stdlib-jdk8"))
        implementation("org.springframework.fu:spring-fu-kofu:0.3.0.M1")
        implementation("io.arrow-kt:arrow-core:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx-reactor:$arrowVersion")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_13.toString()
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Filters dependencies irrelevant for "validation-templates" project
configure(subprojects.filter { it.name != "validation-templates" }) {
    val implementation by configurations
    val testImplementation by configurations
    dependencies {
        implementation(project(":validation-templates"))
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        testImplementation("org.springframework.boot:spring-boot-starter-webflux")
        testImplementation("org.springframework.boot:spring-boot-starter-test") {
            exclude(group = "junit", module = "junit")
        }
    }
}
