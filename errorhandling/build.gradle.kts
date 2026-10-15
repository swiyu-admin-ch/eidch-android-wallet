plugins {
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.swiyu.android.library)
    alias(libs.plugins.mannodermaus.junit)
}

android {
    namespace = "ch.admin.foitt.errorhandling"
}

dependencies {
    // Logging
    implementation(libs.timber)

    // Unit tests
    testImplementation(libs.kotlin.reflect)

    val junitBom = platform(libs.junit.jupiter.bom)
    testImplementation(junitBom)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
