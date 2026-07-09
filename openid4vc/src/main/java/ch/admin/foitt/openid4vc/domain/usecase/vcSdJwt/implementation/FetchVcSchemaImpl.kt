package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchVcSchemaError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toFetchVcSchemaError
import ch.admin.foitt.openid4vc.domain.repository.VcSchemaRepository
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

internal class FetchVcSchemaImpl @Inject constructor(
    private val vcSchemaRepository: VcSchemaRepository,
    private val sriValidator: SRIValidator,
) : FetchVcSchema {
    override suspend fun invoke(schemaUrl: URL, schemaUriIntegrity: String?): Result<VcSchema, FetchVcSchemaError> = coroutineBinding {
        val vcSchemaString = vcSchemaRepository.fetchVcSchema(schemaUrl)
            .mapError(VcSchemaRepositoryError::toFetchVcSchemaError)
            .bind()

        val vcSchema = VcSchema(vcSchemaString)

        schemaUriIntegrity?.let {
            sriValidator(vcSchemaString.encodeToByteArray(), it)
                .mapError(SRIError::toFetchVcSchemaError)
                .bind()
        }

        vcSchema
    }
}
