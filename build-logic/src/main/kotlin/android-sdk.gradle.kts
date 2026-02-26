plugins {
    id("android-common")
    id("com.android.library")
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // minification is done when the whole app is build
            isMinifyEnabled = false
            consumerProguardFiles(
                "consumer-proguard-rules.pro"
            )
        }
    }

    kotlin {
        jvmToolchain(21)
        compilerOptions {
            allWarningsAsErrors.set(true)
        }
    }

    lint {
        lintConfig = file("$rootDir/config/lint/custom-lint.xml")
        quiet = false
        abortOnError = properties.getOrDefault("abortOnLintError", "true") == "true"
        ignoreTestSources = true

        // Warnings
        ignoreWarnings = false
        warningsAsErrors = false

        // If true, running lint on the app module will also run it on all the
        // dependent modules the app depends on. This way, lint has only be
        // invocated once and not again for each module.
        checkDependencies = true

        // Report formats and output paths.
        absolutePaths = false
        xmlReport = true
        htmlReport = true
    }

    testOptions {
        animationsDisabled = true
    }
}
