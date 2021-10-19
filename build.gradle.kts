val javaVersion = JavaVersion.VERSION_16

plugins {
    application
    kotlin("jvm") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("com.github.ben-manes.versions") version "0.39.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))

    // Use the Kotlin test library.
    testImplementation(kotlin("test"))
}

application {
    // Define the main class for the application.
    mainClass.set("no.nav.tpts.mottak.AppKt")
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

// https://github.com/ben-manes/gradle-versions-plugin
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = javaVersion.toString()
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = javaVersion.toString()
    }
    test {
        //JUnit 5 support
        useJUnitPlatform()
    }
    shadowJar {
        dependsOn("test")
        archiveBaseName.set(rootProject.name)
    }
    // https://github.com/ben-manes/gradle-versions-plugin
    dependencyUpdates {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
}
