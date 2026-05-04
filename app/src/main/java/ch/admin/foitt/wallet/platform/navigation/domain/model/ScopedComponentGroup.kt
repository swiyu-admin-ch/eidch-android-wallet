package ch.admin.foitt.wallet.platform.navigation.domain.model

/**
 * Sealed interfaces implementing [ScopedComponentGroup] are used to define a group of [Destination]s as a component scope
 *
 * To create a new component scope, add a new identifier to [ComponentScope].
 * Then create a new sealed interface implementing [ScopedComponentGroup].
 * Let relevant [Destination]s implement the new sealed interface.
 * Finally, update `ComponentScope::contains` to link [ComponentScope] to [ScopedComponentGroup].
 *
 * @see [ch.admin.foitt.wallet.platform.navigation.implementation.DestinationScopedComponentManagerImpl]
 */
sealed interface ScopedComponentGroup {
    sealed interface CredentialIssuer : ScopedComponentGroup
    sealed interface Verifier : ScopedComponentGroup
    sealed interface NonComplianceFormInput : ScopedComponentGroup
    sealed interface EidApplicationProcess : ScopedComponentGroup

    /**
     * [ScopedComponentGroup] for keeping the result of the document scan in memory.
     * Should be kept as small as possible, as the scan files are quite heavy.
     */
    sealed interface EidDocumentScan : ScopedComponentGroup

    /**
     * [ScopedComponentGroup] for keeping the result of the current e-ID online session call in memory.
     */
    sealed interface EidOnlineSession : ScopedComponentGroup

    /**
     * [ScopedComponentGroup] for keeping the otp variables in memory.
     */
    sealed interface Otp : ScopedComponentGroup
}

/**
 * Identifier for component scope
 */
enum class ComponentScope {
    CredentialIssuer,
    Verifier,
    NonComplianceFormInput,
    EidApplicationProcess,
    EidDocumentScan,
    EidOnlineSession,
    Otp,
}

internal fun ComponentScope.contains(destination: Destination?): Boolean = when (this) {
    ComponentScope.CredentialIssuer -> destination is ScopedComponentGroup.CredentialIssuer
    ComponentScope.Verifier -> destination is ScopedComponentGroup.Verifier
    ComponentScope.NonComplianceFormInput -> destination is ScopedComponentGroup.NonComplianceFormInput
    ComponentScope.EidApplicationProcess -> destination is ScopedComponentGroup.EidApplicationProcess
    ComponentScope.EidDocumentScan -> destination is ScopedComponentGroup.EidDocumentScan
    ComponentScope.EidOnlineSession -> destination is ScopedComponentGroup.EidOnlineSession
    ComponentScope.Otp -> destination is ScopedComponentGroup.Otp
}
