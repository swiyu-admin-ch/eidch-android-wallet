package ch.admin.foitt.openid4vc.domain.model.sdjwt.util

import ch.admin.foitt.openid4vc.util.splitNoEmpty
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals

fun assertSelectiveDisclosure(selectiveDisclosure: String, jwt: String, disclosures: List<String>) {
    val split = selectiveDisclosure.splitNoEmpty("~")
    assertEquals(disclosures.size + 1, split.size)
    assertEquals(jwt, split[0])
    disclosures.forEach {
        Assertions.assertTrue(split.contains(it))
    }
}
