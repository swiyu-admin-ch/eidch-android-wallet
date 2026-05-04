package ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.otp.domain.model.OtpVerify
import ch.admin.foitt.wallet.feature.otp.domain.model.RequestOtpError
import ch.admin.foitt.wallet.feature.otp.domain.model.toRequestOtpError
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpRepository
import ch.admin.foitt.wallet.feature.otp.domain.usecase.VerifyOtp
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AppAttestationRepositoryError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.repository.AppAttestationRepository
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class VerifyOtpImpl @Inject constructor(
    private val otpRepository: OtpRepository,
    private val appAttestationRepository: AppAttestationRepository,
    private val requestClientAttestation: RequestClientAttestation,
    private val generateProofOfPossession: GenerateProofOfPossession,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val safeJson: SafeJson,
) : VerifyOtp {
    override suspend fun invoke(otpVerify: OtpVerify): Result<Unit, RequestOtpError> = coroutineBinding {
        val clientAttestation: ClientAttestation = requestClientAttestation()
            .mapError(RequestClientAttestationError::toRequestOtpError).bind()
        val challengeResponse = appAttestationRepository.fetchChallenge()
            .mapError(AppAttestationRepositoryError::toRequestOtpError).bind()
        val requestBody = safeJson.safeEncodeObjectToJsonElement(otpVerify)
            .mapError(JsonParsingError::toRequestOtpError).bind()

        val clientAttestationProofOfPossession: ClientAttestationPoP = generateProofOfPossession(
            clientAttestation = clientAttestation,
            challenge = challengeResponse.challenge,
            audience = environmentSetupRepository.attestationsServiceUrl,
            requestBody = requestBody,
        ).mapError(GenerateProofOfPossessionError::toRequestOtpError).bind()

        otpRepository.verifyOTP(
            clientAttestation = clientAttestation,
            clientAttestationPoP = clientAttestationProofOfPossession,
            otpVerify = otpVerify,
        ).bind()
    }
}
