plugins {
    id("org.jetbrains.kotlin.android")
    id("io.gitlab.arturbosch.detekt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

detekt {
    autoCorrect = false
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    basePath = rootDir.absolutePath
    source.setFrom(
        files(
            "src/abn/",
            "src/dev/",
            "src/main/",
            "src/ref/",
            "src/test/",
        )
    )
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}
