package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyDescriptorMapByPresentationDefinition
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtDescriptorMap
import javax.inject.Inject

internal class CreateAnyDescriptorMapByPresentationDefinitionImpl @Inject constructor(
    private val createVcSdJwtDescriptorMap: CreateVcSdJwtDescriptorMap,
) : CreateAnyDescriptorMapByPresentationDefinition {
    override suspend fun invoke(presentationDefinition: PresentationDefinition): List<DescriptorMap> =
        presentationDefinition.inputDescriptors.map { descriptor ->
            when (descriptor.formats.first()) {
                is InputDescriptorFormat.VcSdJwt -> createVcSdJwtDescriptorMap(
                    descriptor,
                    0 // we only support single credential presentation so far
                )
            }
        }
}
