plugins {
    id("org.springframework.boot") version "2.3.0.M1"
}

val arrowVersion = "0.10.5"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-h2")
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
