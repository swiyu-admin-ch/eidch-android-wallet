package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ValidateIssuerCredentialInfo
import javax.inject.Inject

class ValidateIssuerCredentialInfoImpl @Inject constructor() : ValidateIssuerCredentialInfo {
    override fun invoke(issuerCredentialInfo: IssuerCredentialInfo): Boolean {
        val isResponseEncryptionValid = validateResponseEncryption(
            responseEncryption = issuerCredentialInfo.credentialResponseEncryption,
            requestEncryption = issuerCredentialInfo.credentialRequestEncryption,
        )
        val isRequestEncryptionValid = validateRequestEncryption(
            requestEncryption = issuerCredentialInfo.credentialRequestEncryption,
        )

        return isRequestEncryptionValid && isResponseEncryptionValid
    }

    private fun validateResponseEncryption(
        responseEncryption: CredentialResponseEncryption?,
        requestEncryption: CredentialRequestEncryption?,
    ) = when {
        responseEncryption == null -> true
        requestEncryption == null -> false
        responseEncryption.algValuesSupported.any { it !in supportedAlgorithms } -> false
        responseEncryption.encValuesSupported.any { it !in supportedEncodings } -> false
        responseEncryption.zipValuesSupported?.any { it !in supportedZipValues } == true -> false
        else -> true
    }

    private fun validateRequestEncryption(
        requestEncryption: CredentialRequestEncryption?,
    ) = when {
        requestEncryption == null -> true
        requestEncryption.jwks.keys.any { it.crv !in supportedCurves || it.alg !in supportedAlgorithms } -> false
        requestEncryption.encValuesSupported.any { it !in supportedEncodings } -> false
        requestEncryption.zipValuesSupported?.any { it !in supportedZipValues } == true -> false
        else -> true
    }

    private companion object {
        val supportedAlgorithms = listOf("ECDH-ES")
        val supportedEncodings = listOf("A128GCM")
        val supportedZipValues = listOf("DEF")
        val supportedCurves = listOf("P-256")
    }
}
