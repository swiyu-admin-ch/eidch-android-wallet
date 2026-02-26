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
    implementation("com.android.tools.build:gradle:8.13.2")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.58")

    val kotlinVersion = "2.2.0" // keep in sync with 'devtools-ksp' and 'kotlin' in [libs.versions.toml]
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")

    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
    implementation("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:13.2.1") // keep in sync with version in [libs.versions.toml]
    implementation("com.dynatrace.tools.android:gradle-plugin:8.327.3.1006")
}
