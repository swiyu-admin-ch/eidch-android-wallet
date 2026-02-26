package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import com.github.michaelbull.result.Result
import java.net.URL

interface SaveVcSdJwtCredentials {
    suspend operator fun invoke(
        credentialId: Long = 0,
        issuerUrl: URL,
        vcSdJwtCredentials: List<VcSdJwtCredential>,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        credentialConfig: AnyCredentialConfiguration,
    ): Result<Long, FetchCredentialError>
}
