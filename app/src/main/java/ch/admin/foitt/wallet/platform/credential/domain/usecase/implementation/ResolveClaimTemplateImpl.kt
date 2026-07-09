package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.claimsPathPointerFrom
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.pointsAtSetOf
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.resolveWildCards
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ResolveClaimTemplate
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import javax.inject.Inject

class ResolveClaimTemplateImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : ResolveClaimTemplate {
    override fun invoke(
        template: String,
        claims: List<CredentialClaimWithDisplays>,
        allIndices: List<Int>,
    ): String {
        var resolvedString = template

        val claimsPathPointerFinder = Regex(PATH_PATTERN)
        val claimsPathPointerMatches = claimsPathPointerFinder.findAll(resolvedString).mapNotNull { match ->
            val path = match.groups["path"]?.value ?: return@mapNotNull null
            val separator = match.groups["separator"]?.value ?: DEFAULT_SEPARATOR
            val range = match.range
            Triple(path, separator, range)
        }.toList()

        claimsPathPointerMatches.reversed().forEach { (path, separator, range) ->
            val templateClaimsPathPointer = claimsPathPointerFrom(path)?.resolveWildCards(allIndices)

            val matchingClaims = claims.filter { (claim, _) ->
                val claimClaimsPathPointer = claimsPathPointerFrom(claim.path)
                if (templateClaimsPathPointer != null && claimClaimsPathPointer != null) {
                    templateClaimsPathPointer.pointsAtSetOf(claimClaimsPathPointer, enforceLength = true)
                } else {
                    false
                }
            }.map { (claim, displays) ->
                val display = getLocalizedDisplay(displays)
                display?.value ?: claim.value ?: "–"
            }

            val replacement = matchingClaims.joinToString(separator = separator)

            resolvedString = resolvedString.replaceRange(range, replacement)
        }

        return resolvedString
    }

    private companion object {
        const val DEFAULT_SEPARATOR = ", "

        // matches are claims path pointers surrounded by double curly brackets
        // starts with: {{
        // claims path pointer
        // optional .join(<separator>) to separate when multiple claims are selected
        // ends with: }}
        // ex.: {{["claim", "path", "pointer"].join(', ')}}
        @Suppress("MaximumLineLength")
        const val PATH_PATTERN = """\{\{(?<path>.*?)(?:\.join\((?<q>['"])(?<separator>(?:(?!\k<q>).){0,10})\k<q>\))?\}\}"""
    }
}
