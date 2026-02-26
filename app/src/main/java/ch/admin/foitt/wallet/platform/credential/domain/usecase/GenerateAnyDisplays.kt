package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import com.github.michaelbull.result.Result

interface GenerateAnyDisplays {
    suspend operator fun invoke(
        anyCredential: AnyCredential?,
        issuerInfo: IssuerCredentialInfo,
        trustStatement: TrustStatement? = null,
        metadata: AnyCredentialConfiguration,
        ocaBundle: OcaBundle?,
    ): Result<AnyDisplays, GenerateCredentialDisplaysError>
}
