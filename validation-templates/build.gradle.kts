plugins {
    id("org.jetbrains.kotlin.jvm")
}

val arrowVersion = "0.10.5"
dependencies {
    implementation("io.r2dbc:r2dbc-h2")
    implementation("io.arrow-kt:arrow-mtl:$arrowVersion")
    implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RC1")

    testImplementation("org.junit.jupiter:junit-jupiter:+") {
        exclude("junit", "junit")
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
}


