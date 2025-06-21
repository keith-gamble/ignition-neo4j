import com.github.gradle.node.npm.task.NpmTask

plugins {
    java
    id("com.github.node-gradle.node") version "7.1.0"
}

// This should match the webpack output path
val projectOutput = "build/generated-resources/mounted"

node {
    version.set("20.18.0")
    npmVersion.set("10.8.2")
    download.set(true)
    nodeProjectDir.set(file("$projectDir"))
}

val installDependencies by tasks.registering(NpmTask::class) {
    args.set(listOf("ci"))  // Use 'ci' for faster, reproducible builds
}

val webpack by tasks.registering(NpmTask::class) {
    args.set(listOf("run", "build"))
    dependsOn(installDependencies)
    
    inputs.files(
        project.fileTree(".") {
            exclude("**/node_modules/**", "**/build/**", "**/.awcache/**", "**/yarn-error.log")
        }
    )
    
    outputs.dir(projectOutput)
}

tasks {
    processResources {
        dependsOn(webpack)
    }
    
    clean {
        delete(layout.buildDirectory)
    }
}

val deepClean by tasks.registering {
    doLast {
        delete(file(".gradle"))
        delete(file("node_modules"))
    }
    dependsOn(project.tasks.named("clean"))
}

sourceSets {
    main {
        resources {
            srcDir(projectOutput)
        }
    }
}