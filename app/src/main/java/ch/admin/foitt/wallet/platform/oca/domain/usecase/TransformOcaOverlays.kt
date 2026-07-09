package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay

interface TransformOcaOverlays {
    operator fun invoke(
        overlays: List<Overlay>,
        captureBases: List<CaptureBase>,
    ): List<Overlay>
}
