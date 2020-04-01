plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}

val arrowSnapshotVersion = "0.10.5-SNAPSHOT"
val arrowVersion = "0.10.4"

dependencies {
    implementation("io.r2dbc:r2dbc-h2")
    implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RC1")

    implementation("io.arrow-kt:arrow-core:$arrowSnapshotVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowSnapshotVersion")
    implementation("io.arrow-kt:arrow-fx-reactor:$arrowSnapshotVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:+") {
        exclude(group="junit", module="junit")
    }
}

dependencyManagement {
    imports {
        mavenBom("io.r2dbc:r2dbc-bom:Arabba-SR2")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/snapshot")
    maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}


