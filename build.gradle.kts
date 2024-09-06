plugins {
    id("java")
    id("java-library")
    id("idea")
    id("io.freefair.lombok") version "8.10"
}

group = "example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jspecify:jspecify:1.0.0")
    implementation(libs.spring.boot)
    implementation(libs.spring.boot.test)
    implementation(libs.dynamodb)
    implementation(libs.dynamodb.enhanced)
    implementation(libs.netty.nio.client)
    testImplementation(libs.localstack)
    testImplementation(libs.localstack.junit.jupiter)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}