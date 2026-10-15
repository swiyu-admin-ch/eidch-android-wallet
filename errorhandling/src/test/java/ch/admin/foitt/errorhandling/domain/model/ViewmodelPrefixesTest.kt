package ch.admin.foitt.errorhandling.domain.model

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties

class ViewmodelPrefixesTest {

    @Test
    fun `no duplicate prefix values`() {
        val values = ViewmodelPrefixes::class.declaredMemberProperties
            .map { it.getter.call() as String }

        val duplicates = values
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
            .keys

        assertTrue(duplicates.isEmpty(), "Duplicate prefixes found: $duplicates")
    }
}
