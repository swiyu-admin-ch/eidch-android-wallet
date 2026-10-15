package ch.admin.foitt.errorhandling.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

class ErrorCodesTest {

    @Test
    fun `no two ErrorCode generators produce the same prefix and category`() {
        val containers = listOf(ErrorCodes::class) + ErrorCodes::class.nestedClasses.filter { it.objectInstance != null }

        val allCodes = containers.flatMap { containerClass ->
            val instance = containerClass.objectInstance ?: return@flatMap emptyList()

            containerClass.declaredMemberFunctions
                .filter { it.visibility == KVisibility.PUBLIC }
                .filter { it.parameters.size == 1 }
                .filter { it.returnType.classifier == ErrorCode::class }
                .mapNotNull { function ->
                    function.isAccessible = true
                    (function.call(instance) as? ErrorCode)?.prefixAndCategory()
                }
        }

        assertEquals(
            allCodes.size,
            allCodes.toSet().size,
            "Duplicate prefix+category combinations found: ${
                allCodes.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
            }"
        )
    }

    private fun ErrorCode.prefixAndCategory(): String = value.substringBeforeLast('-')
}
