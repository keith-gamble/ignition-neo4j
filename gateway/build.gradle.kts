plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// Special configuration for bundling third-party libraries
val bundledLibs by configurations.creating

dependencies {
    api(projects.common)
    api(projects.designer)

    // Include the web project's built resources
    modlImplementation(projects.web)

    // Bundle the Neo4J driver with the module
    bundledLibs(libs.neo4j.java.driver)
    implementation(libs.neo4j.java.driver)

    compileOnly(libs.ignition.common)
    compileOnly(libs.ignition.gateway.api)
    compileOnly(libs.ignition.driver.api)
}

// Include bundled libraries in the JAR
tasks.jar {
    from(bundledLibs.map { if (it.isDirectory) it else zipTree(it) })
    
    // Handle duplicate files
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    // Exclude signature files that can cause issues
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}

// Copy web resources from the web project
tasks.register<Copy>("copyWebResources") {
    from(project(":web").layout.buildDirectory.dir("generated-resources/mounted")) {
        include("**/*")
    }
    into("build/resources/main/mounted")
    
    dependsOn(":web:processResources")
}

tasks.named("processResources") {
    dependsOn("copyWebResources")
}