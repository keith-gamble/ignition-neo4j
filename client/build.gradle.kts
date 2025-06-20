plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    api(projects.common)

    compileOnly(libs.ignition.client.api)
    compileOnly(libs.ignition.common)
    compileOnly(libs.ignition.vision.client.api)
}