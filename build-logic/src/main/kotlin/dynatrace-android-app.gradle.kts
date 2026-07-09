plugins {
    id("com.android.application")
    id("com.dynatrace.instrumentation.module")
}

configure<com.dynatrace.tools.android.dsl.DynatraceExtension> {
    configurations {
        create("development") {
            variantFilter("devDebug")
            enabled(false)
        }

        create("sandbox") {
            variantFilter("sandbox")
            enabled(false)
        }

        create("pipeline") {
            autoStart {
                applicationId(properties.getOrDefault("DYNATRACE_APP_ID", "0") as String)
                beaconUrl(properties.getOrDefault("DYNATRACE_BEACON_URL", "https://example.org/mbeacon") as String)
            }
            userOptIn((properties.getOrDefault("DYNATRACE_PRIVACY_OPTIN_MODE", "true") as String).toBoolean())
            debug {
                agentLogging((properties.getOrDefault("DYNATRACE_DEBUG_LOGS", "false") as String).toBoolean())
            }
            userActions {
                enabled(false)
                composeEnabled(false)
            }
            behavioralEvents {
                detectRageTaps(false)
            }
            agentBehavior {
                startupLoadBalancing(true)
            }
            crashReporting(true)
            webRequests.enabled(false)
            lifecycle.enabled(false)
            locationMonitoring(false)
        }
    }
}
