plugins {
    id("android-sdk")
    id("jacoco-android-sdk")
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.junit5)
}

android {
    namespace = "ch.admin.foitt.openid4vc"
    defaultConfig {
        testInstrumentationRunner = "ch.admin.foitt.wallet.CustomTestRunner"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Dagger/Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // ktor
    debugImplementation(libs.slf4j.android)
    releaseImplementation(libs.slf4j.nop)
    implementation(libs.bundles.ktor)

    // JWT
    implementation(libs.nimbus.jose.jwt)

    // DID resolver
    implementation(libs.didresolver)

    // Error handling
    implementation(libs.kotlin.result)
    implementation(libs.kotlin.result.coroutines)

    // Logging
    implementation(libs.timber)

    // Dcql
    implementation(libs.dcql)

    // Testing
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.slf4j.nop)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.ktor.server.core)
    testImplementation(libs.ktor.server.netty)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    // for "Parameterized Tests"
    testImplementation(libs.junit.jupiter.params)
}
