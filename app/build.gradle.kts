import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import com.mikepenz.aboutlibraries.plugin.DuplicateRule

plugins {
    id("android-application")
    id("jacoco-android-app")
    alias(libs.plugins.aboutlibraries.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.junit5)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
}

android {
    namespace = "ch.admin.foitt.wallet"

    val schemeCredentialOffer = "openid-credential-offer"
    val schemeCredentialOfferSwiyu = "swiyu"
    val schemePresentationRequest = "https"
    val schemePresentationRequestOID = "openid4vp"
    val schemePresentationRequestSwiyu = "swiyu-verify"

    defaultConfig {
        applicationId = "ch.admin.foitt.swiyu"
        testApplicationId = "ch.admin.foitt.swiyu.test"
        versionCode = Integer.parseInt(properties.getOrDefault("APP_VERSION_CODE", "1") as String)
        versionName = properties.getOrDefault("APP_VERSION_NAME", "100.0.0") as String
        manifestPlaceholders["appLabel"] = "swiyu"
        manifestPlaceholders["deepLinkCredentialOfferScheme"] = schemeCredentialOffer
        manifestPlaceholders["deepLinkCredentialOfferSchemeSwiyu"] = schemeCredentialOfferSwiyu
        manifestPlaceholders["deepLinkPresentationRequestScheme"] = schemePresentationRequest
        manifestPlaceholders["deepLinkPresentationRequestSchemeOID"] = schemePresentationRequestOID
        manifestPlaceholders["deepLinkPresentationRequestSchemeSwiyu"] = schemePresentationRequestSwiyu

        // keeps only resources in these languages
        // if libs f. e. include resources in spanish they are not shipped with the app
        @Suppress("UnstableApiUsage")
        androidResources.localeFilters += arrayOf("en", "de", "fr", "it", "rm")

        buildConfigField(
            type = "String",
            name = "SCHEME_CREDENTIAL_OFFER",
            value = "\"$schemeCredentialOffer\""
        )
        buildConfigField(
            type = "String",
            name = "SCHEME_CREDENTIAL_OFFER_SWIYU",
            value = "\"$schemeCredentialOfferSwiyu\""
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
            name = "SCHEME_PRESENTATION_REQUEST_SWIYU",
            value = "\"$schemePresentationRequestSwiyu\""
        )

        testInstrumentationRunner = "ch.admin.foitt.wallet.CustomTestRunner"
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
            manifestPlaceholders["appLabel"] = "(DEV) swiyu"
        }

        create("ref") {
            dimension = "environment"
            applicationIdSuffix = ".ref"
            manifestPlaceholders["appLabel"] = "(REF) swiyu"
        }

        create("abn") {
            dimension = "environment"
            applicationIdSuffix = ".abn"
            manifestPlaceholders["appLabel"] = "(ABN) swiyu"
        }

        create("abnstore") {
            dimension = "environment"
            applicationIdSuffix = ".abnstore"
            manifestPlaceholders["appLabel"] = "(ABNSTORE) swiyu"
        }

        create("prod") {
            dimension = "environment"
        }
    }

    applicationVariants.all {
        addJavaSourceFoldersToModel(
            layout.buildDirectory.dir("generated/ksp/$name/kotlin").get().asFile
        )
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")

        getByName("abnstore") {
            java.srcDirs("src/abn/java")
            res.srcDirs("src/abn/res/xml")
        }
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)

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

    implementation(libs.androidx.core.splashscreen)

    // biometrics
    implementation(libs.androidx.biometric)

    // scanner
    implementation(libs.bundles.androidx.camera)
    implementation(libs.zxing.cpp)
    implementation(libs.qrcode.kotlin)

    // security
    implementation(libs.androidx.security.crypto)

    // integrity
    implementation(libs.integrity)

    // Dagger/Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Room / Sqlcipher
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.sqlcipher.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // ktor
    debugImplementation(libs.slf4j.android)
    releaseImplementation(libs.slf4j.nop)
    implementation(libs.bundles.ktor)

    // JSON / JWT
    implementation(libs.nimbus.jose.jwt)
    implementation(libs.json.path)

    // OCA
    implementation(libs.java.json.canonicalization)

    // Logging
    implementation(libs.timber)

    // Error handling
    implementation(libs.kotlin.result)
    implementation(libs.kotlin.result.coroutines)

    // Images
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Animations
    implementation(libs.lottie.compose)

    // AboutLibraries
    implementation(libs.aboutlibraries.core)

    // Json schema validator
    implementation(libs.json.schema.validator)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Unit tests
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.konsist)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.reflect)

    val junitBom = platform(libs.junit.jupiter.bom)
    testImplementation(junitBom)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)

    // Instrumentation tests
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(junitBom)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.junit.vintage.engine)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.ui.automator)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.mockk.android)
    kspAndroidTest(libs.hilt.android.compiler)

    // AvWrapper
    implementation(libs.av.wrapper)
    implementation(libs.java.websocket)

    // Nav3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
}
