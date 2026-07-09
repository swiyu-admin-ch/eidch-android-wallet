package ch.admin.foitt.wallet.platform.pushNotification.domain.model

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP

data class PushClientAttestation(
    val attestation: ClientAttestation,
    val pop: ClientAttestationPoP
)
