pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://nexus.inductiveautomation.com/repository/public")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://nexus.inductiveautomation.com/repository/public")
        }
    }
}

rootProject.name = "neo4j-driver"

// Enable type-safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":common",
    ":gateway", 
    ":client",
    ":designer"
)