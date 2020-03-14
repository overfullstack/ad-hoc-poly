import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.50"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.springframework.boot") version "2.3.0.M1"
}

val arrowSnapshotVersion = "0.10.5-SNAPSHOT"

dependencies {
    implementation("org.springframework.fu:spring-fu-kofu:0.3.0.M1")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-h2")

    implementation("io.arrow-kt:arrow-core:$arrowSnapshotVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowSnapshotVersion")
    implementation("io.arrow-kt:arrow-fx-reactor:$arrowSnapshotVersion")

    implementation(project(":validation-fx"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("io.r2dbc:r2dbc-bom:Arabba-SR2")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
    maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations.all {
    exclude(module = "jakarta.validation-api")
    exclude(module = "hibernate-validator")
    if (project.hasProperty("graal")) {
        exclude(module = "netty-transport-native-epoll")
        exclude(module = "netty-transport-native-unix-common")
        exclude(module = "netty-codec-http2")
    }
}
