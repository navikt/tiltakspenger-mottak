val javaVersion = JavaVersion.VERSION_16

plugins {
    application
    kotlin("jvm") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.0"
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
}
