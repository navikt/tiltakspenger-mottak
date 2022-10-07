val javaVersion = JavaVersion.VERSION_17
val ktorVersion = "2.1.2"
val kotlinxSerializationVersion = "1.4.0"
val kotlinxCoroutinesVersion = "1.6.4"
val prometheusVersion = "0.16.0"
val testContainersVersion = "1.17.3"
val jacksonVersion = "2.13.4"
val mockkVersion = "1.13.2"

plugins {
    val kotlinVersion = "1.7.20"
    application
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.github.ben-manes.versions") version "0.42.0"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("ca.cutterslade.analyze") version "1.9.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
    implementation("ch.qos.logback:logback-classic:1.4.3")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-http-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-utils-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
//    implementation("io.micrometer:micrometer-registry-prometheus:1.8.1")
    implementation("io.prometheus:simpleclient:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("org.apache.avro:avro:1.11.1")
    implementation("io.confluent:kafka-avro-serializer:7.2.2") {
        // we want to use the one provided by R&R, not the Confluent-specific one
        exclude(group = "org.apache.kafka", module = "kafka-clients")
    }
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.github.navikt:rapids-and-rivers:2022100711511665136276.49acbaae4ed4")
    implementation("io.getunleash:unleash-client-java:6.1.0")
    implementation("com.natpryce:konfig:1.6.10.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("io.mockk:mockk-jvm:$mockkVersion")
    testImplementation("io.mockk:mockk-dsl-jvm:$mockkVersion")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test-jvm:$kotlinxCoroutinesVersion")
}

configurations.all {
    // exclude JUnit 4
    exclude(group = "junit", module = "junit")
    // because of https://nav-it.slack.com/archives/C73B9LC86/p1645620641779659
}

application {
    mainClass.set("no.nav.tiltakspenger.mottak.AppKt")
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/config/detekt.yml")
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
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    test {
        // JUnit 5 support
        useJUnitPlatform()
        // https://phauer.com/2018/best-practices-unit-testing-kotlin/
        systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    }
    // https://github.com/ben-manes/gradle-versions-plugin
    dependencyUpdates {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
    analyzeClassesDependencies {
        warnUsedUndeclared = true
        warnUnusedDeclared = true
    }
    analyzeTestClassesDependencies {
        warnUsedUndeclared = true
        warnUnusedDeclared = true
    }
}
