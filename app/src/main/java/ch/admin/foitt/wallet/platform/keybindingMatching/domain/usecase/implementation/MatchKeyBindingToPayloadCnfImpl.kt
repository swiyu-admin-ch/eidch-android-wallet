package ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.toEcJwk
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.openid4vc.domain.model.toCurve
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.Confirmation
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.model.MatchKeyBindingToPayloadCnfError
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.model.toMatchKeyBindingToPayloadCnfError
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase.MatchKeyBindingToPayloadCnf
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.jwk.ECKey
import kotlinx.serialization.json.jsonObject
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject

class MatchKeyBindingToPayloadCnfImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val getHardwareKeyPair: GetHardwareKeyPair,
) : MatchKeyBindingToPayloadCnf {
    override suspend fun invoke(
        keyBindings: List<KeyBinding?>,
        payload: String,
    ): Result<KeyBinding, MatchKeyBindingToPayloadCnfError> = coroutineBinding {
        val keyBindingJwkPairs = keyBindings.filterNotNull().mapNotNull { kb ->
            kb.toJwk()?.let { jwk -> kb to jwk }
        }

        val sdJwt = SdJwt(payload)
        val cnfJsonElement = sdJwt.sdJwtJson.jsonObject["cnf"]
        val confirmation: Confirmation? = cnfJsonElement?.let {
            safeJson.safeDecodeFromJsonElement<Confirmation>(it)
                .mapError(JsonParsingError::toMatchKeyBindingToPayloadCnfError)
                .bind()
        }

        // match the public keys to find the correct key binding
        return@coroutineBinding confirmation?.let { cnf ->
            keyBindingJwkPairs.firstOrNull { (_, jwk) ->
                cnf.jwk.x == jwk.x && cnf.jwk.y == jwk.y
            }?.first
        } ?: Err(
            MatchKeyBindingToPayloadCnfError.Unexpected(
                IllegalStateException(
                    "No matching key binding available."
                )
            )
        ).bind<KeyBinding>()
    }

    private suspend fun KeyBinding.toJwk(): Jwk? {
        val ecPublicKey: ECPublicKey = when (bindingType) {
            KeyBindingType.SOFTWARE -> {
                publicKey?.let { bytes ->
                    val keyFactory = KeyFactory.getInstance("EC")
                    keyFactory.generatePublic(X509EncodedKeySpec(bytes)) as ECPublicKey
                }
            }

            KeyBindingType.HARDWARE -> {
                getHardwareKeyPair(identifier, ANDROID_KEY_STORE).get()?.public as? ECPublicKey
            }
        } ?: return null
        return ECKey.Builder(algorithm.toCurve(), ecPublicKey).build().toEcJwk(certificateChainBase64 = null)
    }
}
