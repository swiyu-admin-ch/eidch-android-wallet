plugins {
    id("jacoco")
    id("com.android.application")
}

dependencies {
    implementation("org.jacoco:org.jacoco.core:0.8.14")
}

project.afterEvaluate {
    val buildTypes = android.buildTypes.map { type -> type.name }
    var productFlavors = android.productFlavors.map { flavor -> flavor.name }
    if (productFlavors.isEmpty()) {
        productFlavors = productFlavors + ""
    }

    // Whitelist by namespace + name pattern
    val includes = setOf(
        "**/domain/usecase/**/*Impl.class",
    )

    productFlavors.forEach { flavorName ->
        buildTypes.forEach { buildTypeName ->

            val sourceName: String = if (flavorName.isEmpty()) {
                buildTypeName
            } else {
                "${flavorName}${buildTypeName.replaceFirstChar(Char::titlecase)}"
            }

            val testTaskName = "test${sourceName.replaceFirstChar(Char::titlecase)}UnitTest"

            registerCodeCoverageTask(
                testTaskName = testTaskName,
                sourceName = sourceName,
                flavorName = flavorName,
                buildTypeName = buildTypeName,
                includes = includes
            )
        }
    }
}

fun Project.registerCodeCoverageTask(
    testTaskName: String,
    sourceName: String,
    flavorName: String,
    buildTypeName: String,
    includes: Set<String>
) {
    tasks.register<JacocoReport>("${testTaskName}Coverage") {
        dependsOn(testTaskName)
        group = "Reporting"
        description = "Generate Jacoco coverage reports on the $sourceName build."

        val javaClasses = fileTree(layout.buildDirectory.dir("intermediates/javac/$sourceName")) {
            include(includes)
        }
        val kotlinClasses = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/$sourceName")) {
            include(includes)
        }

        val coverageSrcDirectories = listOf(
            "src/main/java",
            "src/$flavorName/java",
            "src/$buildTypeName/java",
            "src/main/kotlin",
            "src/$flavorName/kotlin",
            "src/$buildTypeName/kotlin"
        )

        classDirectories.setFrom(files(javaClasses, kotlinClasses))
        additionalClassDirs.setFrom(files(coverageSrcDirectories))
        sourceDirectories.setFrom(files(coverageSrcDirectories))
        executionData.setFrom(
            files(layout.buildDirectory.dir("jacoco/$testTaskName.exec"))
        )

        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
