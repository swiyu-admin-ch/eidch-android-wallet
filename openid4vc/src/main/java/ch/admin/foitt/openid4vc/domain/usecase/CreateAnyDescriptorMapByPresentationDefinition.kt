package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition

internal fun interface CreateAnyDescriptorMapByPresentationDefinition {
    suspend operator fun invoke(presentationDefinition: PresentationDefinition): List<DescriptorMap>
}
