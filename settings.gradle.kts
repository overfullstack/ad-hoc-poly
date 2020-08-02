rootProject.name = "ad-hoc-poly"

include(
        "kofu-mvc-validation",
        "kofu-reactive-validation",
        "kofu-coroutines-validation",
        "validation-templates"
)

pluginManagement {
    repositories {
        jcenter()
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
        maven("https://repo.spring.io/milestone")
        maven("https://repo.spring.io/snapshot")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
    val kotlinEap: String by settings
    plugins {
        kotlin("jvm") version kotlinEap // This is handy if there are multiple modules. This lets you declare version at one place.
    }
}
