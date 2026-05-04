import org.gradle.kotlin.dsl.implementation

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.hilt.android.gradle.plugin)

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization)

    implementation(libs.detekt.gradle.plugin)
    implementation(libs.aboutlibraries.plugin)
    implementation(libs.dynatrace.gradle.plugin)
}
