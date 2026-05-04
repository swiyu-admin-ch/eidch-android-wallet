package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.PreAuthorizedContent
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.proofTypeConfigHardwareBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.proofTypeConfigSoftwareBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.vcSdJwtCredentialConfiguration
import java.net.URL

internal object MockCredentialOffer {
    val CREDENTIAL_ISSUER = URL("https://issuer.example.com")
    const val CREDENTIAL_IDENTIFIER = "credentialIdentifier"
    private val CREDENTIALS = listOf(CREDENTIAL_IDENTIFIER)
    private const val PRE_AUTHORIZED_CODE = "preAuthorizedCode"
    val offerWithPreAuthorizedCode = CredentialOffer(
        credentialIssuer = CREDENTIAL_ISSUER,
        credentialConfigurationIds = CREDENTIALS,
        grants = Grant(preAuthorizedCode = PreAuthorizedContent(PRE_AUTHORIZED_CODE))
    )
    val offerWithoutPreAuthorizedCode = offerWithPreAuthorizedCode.copy(grants = Grant())
    val offerWithoutMatchingCredentialIdentifier =
        offerWithPreAuthorizedCode.copy(credentialConfigurationIds = listOf("otherCredentialIdentifier"))

    const val KEY_ATTESTATION_JWT = "keyAttestationJwt"
    private const val ACCESS_TOKEN = "accessToken"
    private const val REFRESH_TOKEN = "refreshToken"
    const val C_NONCE = "cNonce"
    private const val EXPIRES_IN = 2
    private const val TOKEN_TYPE = "tokenType"
    private const val POLLING_INTERVAL = 1
    val validTokenResponse = TokenResponse(
        accessToken = ACCESS_TOKEN,
        expiresIn = EXPIRES_IN,
        tokenType = TOKEN_TYPE
    )
    val TEST_JWT =
        Jwt(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJ0ZXN0X2tleSI6InRlc3RfdmFsdWUifQ.jEtCE0H-VZuMRuAzE6Jc-brd2bfbs_kS7ptlNOmcs1Q5AdZMlm4JTcwwCAwvELZ-FKdDfG-rnuVNoK5gRa6g2Q"
        )

    private val TOKEN_ENDPOINT = URL("$CREDENTIAL_ISSUER/token")
    val validIssuerConfig = IssuerConfiguration(
        issuer = CREDENTIAL_ISSUER,
        tokenEndpoint = TOKEN_ENDPOINT
    )

    private val CREDENTIAL_ENDPOINT = URL("$CREDENTIAL_ISSUER/credential")
    private val DEFERRED_CREDENTIAL_ENDPOINT = URL("$CREDENTIAL_ISSUER/deferred")
    private val NONCE_ENDPOINT = URL("$CREDENTIAL_ISSUER/nonce")

    val validIssuerCredentialInfo = IssuerCredentialInfo(
        credentialEndpoint = CREDENTIAL_ENDPOINT,
        credentialIssuer = CREDENTIAL_ISSUER,
        credentialConfigurations = listOf(vcSdJwtCredentialConfiguration),
        credentialRequestEncryption = null,
        credentialResponseEncryption = null,
        display = listOf(),
        deferredCredentialEndpoint = DEFERRED_CREDENTIAL_ENDPOINT,
        nonceEndpoint = NONCE_ENDPOINT,
    )

    const val KEY_ID = "keyId"
    private const val PROOF_JWT = "proofJwt"
    val jwtProofs = CredentialRequestProofsJwt(listOf(PROOF_JWT))

    private val ALGORITHM = SigningAlgorithm.ES512

    const val CREDENTIAL = "credential"
    private const val TRANSACTION_ID = "transaction_id"
    private const val NOTIFICATION_ID = "notification_id"
    val validCredentialResponse = CredentialResponse.VerifiableCredential(
        credentials = listOf(CredentialResponse.Credential(CREDENTIAL)),
        notificationId = NOTIFICATION_ID,
    )

    val validDeferredCredentialResponse = CredentialResponse.DeferredCredential(
        transactionId = TRANSACTION_ID,
        interval = 1,
    )

    private val keyBinding = KeyBinding(
        identifier = KEY_ID,
        algorithm = ALGORITHM,
        bindingType = KeyBindingType.SOFTWARE,
    )

    val validVerifiableCredential = VerifiableCredential(
        credential = CREDENTIAL,
        keyBinding = keyBinding
    )

    val validDeferredCredential = DeferredCredential(
        format = CredentialFormat.VC_SD_JWT,
        keyBindings = listOf(keyBinding),
        transactionId = TRANSACTION_ID,
        accessToken = ACCESS_TOKEN,
        refreshToken = REFRESH_TOKEN,
        endpoint = DEFERRED_CREDENTIAL_ENDPOINT,
        pollInterval = POLLING_INTERVAL,
    )

    val verifiableCredentialParamsSoftwareBinding = VerifiableCredentialParams(
        proofTypeConfig = proofTypeConfigSoftwareBinding,
        tokenEndpoint = validIssuerConfig.tokenEndpoint,
        grants = offerWithPreAuthorizedCode.grants,
        issuerEndpoint = offerWithPreAuthorizedCode.credentialIssuer,
        credentialEndpoint = validIssuerCredentialInfo.credentialEndpoint,
        deferredCredentialEndpoint = null,
        credentialConfiguration = vcSdJwtCredentialConfiguration,
        nonceEndpoint = NONCE_ENDPOINT,
        isBatch = false,
    )

    val verifiableCredentialParamsHardwareBinding = VerifiableCredentialParams(
        proofTypeConfig = proofTypeConfigHardwareBinding,
        tokenEndpoint = validIssuerConfig.tokenEndpoint,
        grants = offerWithPreAuthorizedCode.grants,
        issuerEndpoint = offerWithPreAuthorizedCode.credentialIssuer,
        credentialEndpoint = validIssuerCredentialInfo.credentialEndpoint,
        deferredCredentialEndpoint = null,
        credentialConfiguration = vcSdJwtCredentialConfiguration,
        nonceEndpoint = NONCE_ENDPOINT,
        isBatch = false,
    )

    val verifiableCredentialParamsWithoutBinding = VerifiableCredentialParams(
        proofTypeConfig = null,
        tokenEndpoint = validIssuerConfig.tokenEndpoint,
        grants = offerWithPreAuthorizedCode.grants,
        issuerEndpoint = offerWithPreAuthorizedCode.credentialIssuer,
        credentialEndpoint = validIssuerCredentialInfo.credentialEndpoint,
        deferredCredentialEndpoint = null,
        credentialConfiguration = vcSdJwtCredentialConfiguration,
        nonceEndpoint = null,
        isBatch = false,
    )

    val verifiableDeferredCredentialParamsHardwareBinding = VerifiableCredentialParams(
        proofTypeConfig = proofTypeConfigHardwareBinding,
        tokenEndpoint = validIssuerConfig.tokenEndpoint,
        grants = offerWithPreAuthorizedCode.grants,
        issuerEndpoint = offerWithPreAuthorizedCode.credentialIssuer,
        credentialEndpoint = validIssuerCredentialInfo.credentialEndpoint,
        deferredCredentialEndpoint = validIssuerCredentialInfo.deferredCredentialEndpoint,
        credentialConfiguration = vcSdJwtCredentialConfiguration,
        nonceEndpoint = NONCE_ENDPOINT,
        isBatch = false,
    )
}
