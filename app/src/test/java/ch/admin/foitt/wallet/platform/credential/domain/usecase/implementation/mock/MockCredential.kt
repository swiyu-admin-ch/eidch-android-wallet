package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential

object MockCredential {
    val vcSdJwtCredentialProd = VcSdJwtCredential(
        id = 1L,
        payload = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImtleUlkIn0.eyJpc3MiOiJkaWQ6dGR3OmZvbz06aWRlbnRpZmllci1yZWcudHJ1c3QtaW5mcmEuc3dpeXUuYWRtaW4uY2g6YmFyIiwidmN0IjoidmN0In0.DJSe-i0SzYUism-XlBolB8MVklhTaijqAbAQCpC3piRp_h2Lo9f6k1AZuG-ytptuWgRtUIJ69EFVTeZFP6sCRw"
    )

    val vcSdJwtCredentialBeta = VcSdJwtCredential(
        id = 1L,
        payload = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImtleUlkIn0.eyJpc3MiOiJkaWQ6dGR3OmZvbz06aWRlbnRpZmllci1yZWcudHJ1c3QtaW5mcmEuc3dpeXUtaW50LmFkbWluLmNoOmJhciIsInZjdCI6InZjdCJ9.eMHxpAcopjZ-BnJnb26NM6yaTJAZyfwt7TgJt4bLAs4fyPHc4dHMk8NkEebZLNQRyPm4MsexyIfbEYcP-eUXbQ"
    )
}
