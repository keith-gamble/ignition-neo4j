plugins {
    alias(libs.plugins.ignition.module)
    id("eclipse") // Added for better IDE support with Eclipse & VS Code
}

allprojects {
    apply(plugin = "eclipse")
}

ignitionModule {
    name.set("Neo4J Driver")
    fileName.set("Neo4J-Driver.modl")
    id.set("dev.bwdesigngroup.neo4j.Neo4JDriver")
    moduleVersion.set(findProperty("version")?.toString() ?: "0.0.0-SNAPSHOT")
    moduleDescription.set("Adds a Neo4J driver connection for the client and gateway with modern Neo4J 5.x support.")
    requiredIgnitionVersion.set(libs.versions.ignition.get())

    projectScopes.putAll(
        mapOf(
            ":client" to "C",
            ":common" to "GCD",
            ":designer" to "D",
            ":gateway" to "G"
        )
    )

    moduleDependencies.putAll(emptyMap())

    hooks.putAll(
        mapOf(
            "dev.bwdesigngroup.neo4j.gateway.Neo4JDriverGatewayHook" to "G",
            "dev.bwdesigngroup.neo4j.client.Neo4JDriverClientHook" to "C",
            "dev.bwdesigngroup.neo4j.designer.Neo4JDriverDesignerHook" to "D"
        )
    )

    applyInductiveArtifactRepo.set(true)
    skipModlSigning.set(!findProperty("signModule").toString().toBoolean())
}

tasks.withType<io.ia.sdk.gradle.modl.task.Deploy>().configureEach {
  hostGateway.set(project.findProperty("hostGateway")?.toString() ?: "")
}

val deepClean by
    tasks.registering {
      dependsOn(allprojects.map { "${it.path}:clean" })
      description = "Executes clean tasks and remove node plugin caches."
      doLast { delete(file(".gradle")) }
    }