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
    api(projects.client)

    compileOnly(libs.ignition.designer.api)
    compileOnly(libs.ignition.common)
}