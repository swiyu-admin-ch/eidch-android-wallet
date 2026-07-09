package ch.admin.foitt.wallet.util

import android.os.Build
import org.junit.Assume
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Classes that inherit from [RealDeviceTest] can annotate test functions with @[SkipTestOnEmulator].
 */
abstract class RealDeviceTest {
    @get:Rule
    val realDeviceRule = RealDeviceRule()

    class RealDeviceRule : TestRule {
        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    val annotation = description.getAnnotation(SkipTestOnEmulator::class.java)
                    if (annotation != null) {
                        Assume.assumeTrue("Skipped test because run on emulator", !isEmulator())
                    }
                    base.evaluate()
                }
            }
        }

        private fun isEmulator() = (
            Build.FINGERPRINT.contains("generic") ||
                Build.BRAND.contains("generic") ||
                Build.DEVICE.contains("generic") ||
                Build.FINGERPRINT.contains("google_sdk") ||
                Build.MODEL.contains("sdk")
            )
    }
}

/**
 * Test classes with this annotation are skipped if they run on an emulator
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SkipTestOnEmulator
