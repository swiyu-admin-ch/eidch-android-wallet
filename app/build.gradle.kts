import com.android.build.api.dsl.ApplicationProductFlavor
import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import com.mikepenz.aboutlibraries.plugin.DuplicateRule
import project.convention.logic.GenerateDebugLocaleTask

plugins {
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.swiyu.android.application)

    id("com.google.dagger.hilt.android")
    alias(libs.plugins.mannodermaus.junit)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.google.services)

    kotlin("plugin.serialization") version "2.3.21"
    alias(libs.plugins.aboutlibraries)

}

android {
    namespace = "ch.admin.foitt.wallet"

    val schemeCredentialOffer = "openid-credential-offer"
    val schemePresentationRequest = "https"
    val schemePresentationRequestOID = "openid4vp"
    val schemePresentationRequestProximity = "mdoc"

    defaultConfig {
        applicationId = "ch.admin.foitt.swiyu"
        testApplicationId = "ch.admin.foitt.swiyu.test"
        versionCode = Integer.parseInt(properties.getOrDefault("APP_VERSION_CODE", "1") as String)
        versionName = properties.getOrDefault("APP_VERSION_NAME", "100.0.0") as String
        manifestPlaceholders["appLabel"] = "swiyu"
        manifestPlaceholders["deepLinkCredentialOfferScheme"] = schemeCredentialOffer
        manifestPlaceholders["deepLinkPresentationRequestScheme"] = schemePresentationRequest
        manifestPlaceholders["deepLinkPresentationRequestSchemeOID"] = schemePresentationRequestOID
        manifestPlaceholders["deepLinkPresentationRequestSchemeProximity"] = schemePresentationRequestProximity

        buildConfigField(
            type = "String",
            name = "SCHEME_CREDENTIAL_OFFER",
            value = "\"$schemeCredentialOffer\""
        )
        buildConfigField(
            type = "String",
            name = "SCHEME_PRESENTATION_REQUEST",
            value = "\"$schemePresentationRequest\""
        )
        buildConfigField(
            type = "String",
            name = "SCHEME_PRESENTATION_REQUEST_OID",
            value = "\"$schemePresentationRequestOID\""
        )
        buildConfigField(
            type = "String",
            name = "SCHEME_PRESENTATION_REQUEST_PROXIMITY",
            value = "\"$schemePresentationRequestProximity\""
        )

        buildConfigField(
            type = "boolean",
            name = "DEBUG_LOCALE_ENABLED",
            value = "false"
        )
    }

    signingConfigs {
        create("release") {
            storeFile = properties["RELEASE_STORE_FILE"]?.let { file(it) }
            storePassword = properties["RELEASE_STORE_PASSWORD"] as String?
            keyAlias = properties["RELEASE_KEY_ALIAS"] as String?
            keyPassword = properties["RELEASE_KEY_PASSWORD"] as String?
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            manifestPlaceholders["appLabel"] = "swiyu (DEV)"
            applyFlavorDeeplinkSchemes()
        }

        create("ref") {
            dimension = "environment"
            applicationIdSuffix = ".ref"
            manifestPlaceholders["appLabel"] = "swiyu (REF)"
            applyFlavorDeeplinkSchemes()
            buildConfigField(
                type = "boolean",
                name = "DEBUG_LOCALE_ENABLED",
                value = "true"
            )
        }

        create("abn") {
            dimension = "environment"
            applicationIdSuffix = ".abn"
            manifestPlaceholders["appLabel"] = "swiyu (ABN)"
            applyFlavorDeeplinkSchemes()
        }

        create("sandbox") {
            dimension = "environment"
            applicationIdSuffix = ".sandbox"
            manifestPlaceholders["appLabel"] = "swiyu Sandbox Wallet"
            applyFlavorDeeplinkSchemes("-sandbox")
            ndk {
                // integrators using the sandbox wallet hopefully use a somewhat current device
                abiFilters += listOf("arm64-v8a")
            }
        }

        create("prod") {
            dimension = "environment"
            applyFlavorDeeplinkSchemes()
        }
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.directories.addAll(listOf("$projectDir/schemas"))
    }
}

fun ApplicationProductFlavor.applyFlavorDeeplinkSchemes(
    environmentAppendix: String = ""
) {
    val schemeCredentialOfferSwiyu = "swiyu$environmentAppendix"
    val schemePresentationRequestSwiyu = "swiyu-verify$environmentAppendix"

    manifestPlaceholders["deepLinkCredentialOfferSchemeSwiyu"] = schemeCredentialOfferSwiyu
    manifestPlaceholders["deepLinkPresentationRequestSchemeSwiyu"] = schemePresentationRequestSwiyu

    buildConfigField(
        type = "String",
        name = "SCHEME_CREDENTIAL_OFFER_SWIYU",
        value = "\"$schemeCredentialOfferSwiyu\""
    )
    buildConfigField(
        type = "String",
        name = "SCHEME_PRESENTATION_REQUEST_SWIYU",
        value = "\"$schemePresentationRequestSwiyu\""
    )
}

androidComponents {
    onVariants(selector().withFlavor("environment" to "ref")) { variant ->
        val taskName = "generate${variant.name.replaceFirstChar { it.uppercase() }}DebugLocale"
        val task = tasks.register(taskName, GenerateDebugLocaleTask::class) {
            baseStrings.set(layout.projectDirectory.file("src/main/res/values/strings.xml"))
        }
        variant.sources.res?.addGeneratedSourceDirectory(task, GenerateDebugLocaleTask::outputDir)
    }
}

aboutLibraries {
    library {
        duplicationMode = DuplicateMode.MERGE
        duplicationRule = DuplicateRule.GROUP
    }
}

dependencies {
    implementation(project(":theme"))
    implementation(project(":openid4vc"))
    implementation(project(":errorhandling"))

    // AvWrapper
    implementation(libs.av.wrapper)
    implementation(libs.java.websocket)

    // Dcql
    implementation(libs.dcql)

    // Nav3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.androidx.adaptive)

    // biometrics
    implementation(libs.androidx.biometric)

    // scanner
    implementation(libs.bundles.androidx.camera)
    implementation(libs.zxing.cpp)
    implementation(libs.qrcode.kotlin)

    // permissions
    implementation(libs.accompanist.permissions)

    // security
    implementation(libs.androidx.security.crypto)

    // Dagger/Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.hilt.android.gradle.plugin)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // ktor
    debugImplementation(libs.slf4j.android)
    releaseImplementation(libs.slf4j.nop)
    implementation(libs.bundles.ktor)
    testImplementation(libs.ktor.client.mock)

    // JSON / JWT
    implementation(libs.nimbus.jose.jwt)

    // OCA
    implementation(libs.java.json.canonicalization)

    // Logging
    implementation(libs.timber)

    // Error handling
    implementation(libs.kotlin.result)
    implementation(libs.kotlin.result.coroutines)

    // Firebase
    val firebaseBom = platform(libs.firebase.bom)
    implementation(firebaseBom)
    implementation(libs.firebase.messaging)

    // Images
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Animations
    implementation(libs.lottie.compose)

    // AboutLibraries
    implementation(libs.aboutlibraries.core)

    // Json schema validator
    implementation(libs.json.schema.validator)

    // Proximity
    implementation(libs.proximity)

    // Debug tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Unit tests
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.konsist)
    testImplementation(libs.mockk)

    val junitBom = platform(libs.junit.jupiter.bom)
    testImplementation(junitBom)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Instrumentation tests
    androidTestImplementation(junitBom)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
}
