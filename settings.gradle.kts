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
        
        // Declare the Node.js download repository
        ivy {
            name = "Node.js"
            setUrl("https://nodejs.org/dist/")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
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
    ":designer",
    ":web" 
)