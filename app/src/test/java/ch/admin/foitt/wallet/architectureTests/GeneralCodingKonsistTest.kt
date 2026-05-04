package ch.admin.foitt.wallet.architectureTests

import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.app.MainActivity
import ch.admin.foitt.wallet.app.WalletApplication
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.EntryProviderInstaller
import ch.admin.foitt.wallet.util.assertTrue
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withOpenModifier
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.ext.provider.hasAnnotationOf
import com.lemonappdev.konsist.api.ext.provider.hasParentOf
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import dagger.Module
import dagger.assisted.AssistedFactory
import dagger.multibindings.IntoSet
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import javax.inject.Inject

class GeneralCodingKonsistTest {
    val completeEntryProviderCode = Konsist
        .scopeFromProject()
        .functions()
        .filter { it.returnType?.name == "EntryProviderInstaller" }.joinToString { it.text }

    @Test
    fun `unit test should not use JUnit4`() {
        Konsist
            .scopeFromTest()
            .classes()
            .filterNot { "androidTest" in it.path }
            .functions()
            .assertFalse {
                it.annotations.any { annotation ->
                    annotation.fullyQualifiedName == "org.junit.Test"
                }
            }
    }

    @Test
    fun `Database must use sqlcipher SupportOpenHelperFactory`() {
        // This only tests for the import of the sqlcipher.SupportOpenHelperFactory
        // Actual use of it is tested by Detekt that will throw an error when imports are not used
        val hasCorrectImport = Konsist
            .scopeFromFile("app/src/main/java/ch/admin/foitt/wallet/platform/database/data/SqlCipherDatabaseInitializer.kt")
            .imports
            .any {
                it.name == "net.zetetic.database.sqlcipher.SupportOpenHelperFactory"
            }
        assertTrue(hasCorrectImport) {
            "SqlCipherDatabaseInitializer must import and use sqlcipher.SupportOpenHelperFactory"
        }
    }

    @Test
    fun `Hilt modules providing EntryProviderInstaller must be annotated with @IntoSet`() {
        Konsist
            .scopeFromProject()
            .classesAndObjects()
            .filter { it.hasAnnotationOf<Module>() }
            .functions()
            .filter { it.returnType == EntryProviderInstaller::class.java }
            .assertTrue {
                it.hasAnnotationOf<IntoSet>()
            }
    }

    @Test
    fun `All destinations must be provided by an EntryProviderInstaller`() {
        Konsist
            .scopeFromProject()
            .classesAndObjects()
            .filter { it.hasParentOf<Destination>() }
            .map {
                it.assertTrue(
                    additionalMessage = "${it.fullyQualifiedName} is not referenced in any EntryProviderInstallerModule"
                ) { destination ->
                    completeEntryProviderCode.contains(destination.name)
                }
            }
    }

    @Test
    fun `ViewModel_AssistedFactory must be used to create correct ViewModel`() {
        Konsist
            .scopeFromProject()
            .interfaces()
            .filter { it.hasAnnotationOf<AssistedFactory>() }
            .mapNotNull { it.fullyQualifiedName?.split(".")?.takeLast(2)?.joinToString(".") }
            .map { assistedFactory: String ->
                assertTrue(
                    completeEntryProviderCode.contains(assistedFactory),
                    "$assistedFactory not used to create entry in any EntryProviderInstallerModule. This will crash during runtime."
                )
            }
    }

    @Test
    fun `Destination screen name must end with Screen and a Composable screen with the same name must exist`() {
        val allScreenNames = Konsist
            .scopeFromProject()
            .functions()
            .withAnnotationOf(Composable::class)
            .filter { it.name.endsWith("Screen") }
            .map { it.name }

        Konsist
            .scopeFromProject()
            .classesAndObjects()
            .filter { it.hasParentOf<Destination>() }
            .map {
                it.assertTrue(
                    additionalMessage = "${it.fullyQualifiedName} does not end with Screen"
                ) { destination ->
                    destination.name.endsWith("Screen")
                }
                it.assertTrue(
                    additionalMessage = "${it.fullyQualifiedName} does not refer to a Compose screen with the same name"
                ) { destination ->
                    destination.name in allScreenNames
                }
            }
    }

    @Test
    fun `instrumentation tests should not use JUnit5`() {
        Konsist
            .scopeFromTest()
            .classes()
            .filter { "androidTest" in it.path }
            .functions()
            .assertFalse(additionalMessage = "Bitbar cannot run JUnit 5 tests") {
                it.annotations.any { annotation ->
                    annotation.fullyQualifiedName == "org.junit.jupiter.api.Test"
                }
            }
    }

    @Test
    fun `no class should use field injection`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filterNot {
                // Exceptions: Application and MainActivity
                it.fullyQualifiedName?.let { name ->
                    name in listOf(
                        WalletApplication::class.java.name,
                        MainActivity::class.java.name
                    )
                } ?: false
            }
            .properties()
            .assertFalse { it.hasAnnotationOf<Inject>() }
    }

    @Test
    fun `Timber error and warning (this) should not be used`() {
        Konsist
            .scopeFromProject()
            .files
            .filter { it.path.endsWith(".kt") }
            .filterNot { "androidTest" in it.path || "test" in it.path }
            .assertFalse(additionalMessage = "Use the following format: Timber.x(t = throwable, message = \"Some context\")") {
                it.text.contains("Timber.e(this)") || it.text.contains("Timber.w(this)")
            }
    }

    @Test
    fun `Serializable classes should not be open`() = Konsist
        .scopeFromProject()
        .classes()
        .withOpenModifier()
        .assertFalse(additionalMessage = "This will create undetected issues with subclasses") {
            it.hasAnnotationWithName("Serializable")
        }

    @TestFactory
    fun `check for 'blacklisted' methods or classes`(): Stream<DynamicTest> =
        Konsist
            .scopeFromProduction()
            .files
            .stream()
            .flatMap { file ->
                Stream.of(
                    DynamicTest.dynamicTest("${file.name}: compose.material should not be used") {
                        file.assertFalse(additionalMessage = "use 'compose.material3' instead") {
                            it.hasImport { import ->
                                // Exceptions: for now we allow material.pullrefresh and material.ExperimentalApi
                                import.hasNameMatching(
                                    "androidx\\.compose\\.material\\.(?!pullrefresh|ExperimentalMaterialApi).*".toRegex()
                                )
                            }
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: collectAsState() should not be used") {
                        file.assertFalse(additionalMessage = "use 'collectAsStateWithLifecycle' instead") {
                            it.hasImportWithName("androidx.compose.runtime.collectAsState")
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: vanilla Kotlin Result should not be used") {
                        file.assertFalse(additionalMessage = "use 'michaelbull.result instead'") {
                            it.hasImportWithName("kotlin.Result")
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: java.util.logging and android.util.Log should not be used") {
                        file.assertFalse(additionalMessage = "use 'Timber' instead") {
                            it.hasImportWithName(listOf("java.util.logging..", "android.util.Log"))
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: sharedPreferences should not be used") {
                        file.assertFalse(additionalMessage = "use 'encryptedSharedPreferences' instead") {
                            it.hasImportWithName("android.content.sharedPreferences")
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: android.util.Base64 should not be used") {
                        file.assertFalse {
                            it.hasImportWithName("android.util.Base64")
                        }
                    },
                    DynamicTest.dynamicTest(
                        "${file.name}: java.util.Base64 should not be used outside of 'platform_utils' package or 'openid4vc' module"
                    ) {
                        if (!file.hasPackage("..platform.utils..") && !file.resideInModule("openid4vc")) {
                            file.assertFalse {
                                it.hasImportWithName("java.util.Base64")
                            }
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: RoomDatabase should not be used outside of 'platform_database' package") {
                        if (!file.hasPackage("..platform.database..")) {
                            file.assertFalse {
                                it.hasImportWithName("androidx.room.RoomDatabase")
                            }
                        }
                    },
                    DynamicTest.dynamicTest("${file.name}: kotlinx.serialization.json.Json methods should not be used") {
                        val exceptions =
                            listOf("SafeJson", "OpenId4VcModule", "UtilModule", "Jwt", "SdJwt", "Migration15To16")
                        if (!file.hasClassWithName(names = exceptions) && file.name != "ClaimsPathPointer") {
                            file.assertFalse(additionalMessage = "use methods from 'SafeJson' instead") { fileDeclaration ->
                                fileDeclaration.hasImport { import ->
                                    import.name == "kotlinx.serialization.json.Json" ||
                                        import.name.startsWith("kotlinx.serialization.json.Json.", ignoreCase = false)
                                }
                            }
                        }
                    },
                )
            }
}
