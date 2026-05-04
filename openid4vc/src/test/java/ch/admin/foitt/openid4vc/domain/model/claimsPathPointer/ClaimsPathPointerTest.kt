package ch.admin.foitt.openid4vc.domain.model.claimsPathPointer

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class ClaimsPathPointerTest {

    @TestFactory
    fun `Claim path pointer is correctly transformed to string`(): List<DynamicTest> {
        return validPointerPairs.map { (jsonString, claimsPathPointer) ->
            DynamicTest.dynamicTest("Claims path pointer $claimsPathPointer should return $jsonString") {
                runTest {
                    val result = claimsPathPointer.toPointerString()

                    assertEquals(jsonString, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer is correctly parsed from string`(): List<DynamicTest> {
        return validPointerPairs.map { (jsonString, claimsPathPointer) ->
            DynamicTest.dynamicTest("Claims path pointer from $jsonString should return $claimsPathPointer") {
                runTest {
                    val result = claimsPathPointerFrom(jsonString)

                    assertEquals(claimsPathPointer, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer is not parsed from invalid string`(): List<DynamicTest> {
        val invalidPointers = listOf(
            "",
            "name",
            "[\"name\" null]",
            "{\"name\": null}",
            "[-1]",
            "[null, -1]",
            "[\"name\", null, -1]",
        )
        return invalidPointers.map { jsonString ->
            DynamicTest.dynamicTest("Claims path pointer from $jsonString should not return a claims path pointer") {
                runTest {
                    val result = claimsPathPointerFrom(jsonString)

                    assertEquals(null, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer points at its own set`(): List<DynamicTest> {
        return validPointerPairs.map { (_, pointer) ->
            DynamicTest.dynamicTest("Claims path pointer $pointer should be subset of itself") {
                runTest {
                    val result = pointer.pointsAtSetOf(pointer)

                    assertEquals(true, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer with null component points at set of pointer with index component`(): List<DynamicTest> {
        val pointerPairs = mapOf(
            listOf(ClaimsPathPointerComponent.Null) to listOf(ClaimsPathPointerComponent.Index(0)),
            listOf(ClaimsPathPointerComponent.Null) to listOf(ClaimsPathPointerComponent.Index(1)),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.Index(0), ClaimsPathPointerComponent.Index(0)),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.Index(0)),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.Index(0), ClaimsPathPointerComponent.Null),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Index(0), ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.Index(0)),
        )
        return pointerPairs.map { (pointer, targetPointer) ->
            DynamicTest.dynamicTest("Claims path pointer with index $pointer should be subset of pointer with null $targetPointer") {
                runTest {
                    val result = pointer.pointsAtSetOf(targetPointer)

                    assertEquals(true, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer which does not point at a set of another returns false`(): List<DynamicTest> {
        val pointerPairs = mapOf(
            listOf(ClaimsPathPointerComponent.String("name")) to listOf(
                ClaimsPathPointerComponent.String("otherName")
            ),
            listOf(ClaimsPathPointerComponent.String("name")) to listOf(
                ClaimsPathPointerComponent.Index(1)
            ),
            listOf(ClaimsPathPointerComponent.String("name")) to listOf(
                ClaimsPathPointerComponent.Null
            ),
            listOf(ClaimsPathPointerComponent.Index(1)) to listOf(ClaimsPathPointerComponent.Index(0)),
            listOf(ClaimsPathPointerComponent.Index(1)) to listOf(ClaimsPathPointerComponent.String("name")),
            listOf(ClaimsPathPointerComponent.Index(1)) to listOf(ClaimsPathPointerComponent.Null),
            listOf(ClaimsPathPointerComponent.Null) to listOf(ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.String("otherName"), ClaimsPathPointerComponent.String("otherName")),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.String("otherName")),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.String("otherName"), ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.Index(1)),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.Null),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Index(0), ClaimsPathPointerComponent.Index(0)),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.Index(0)),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Index(0), ClaimsPathPointerComponent.Index(1)),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.Index(1)),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.Null),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.Null),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.Index(1)),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.String("otherName")),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Index(0), ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.Null),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.Index(1),
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.Null),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.Index(0)),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.String("otherName"), ClaimsPathPointerComponent.Index(1)),
            listOf(
                ClaimsPathPointerComponent.String("name"),
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.Null),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.String("name")),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.Null
            ) to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.Index(1)),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.String("otherName")),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.Index(1)),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.String("name")
            ) to listOf(ClaimsPathPointerComponent.Index(0), ClaimsPathPointerComponent.String("otherName")),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.Null),
            listOf(
                ClaimsPathPointerComponent.Null,
                ClaimsPathPointerComponent.Index(1)
            ) to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.String("name")),
        )
        return pointerPairs.map { (pointer, targetPointer) ->
            DynamicTest.dynamicTest("Claims path pointer $pointer should not be subset of pointer $targetPointer") {
                runTest {
                    val result = pointer.pointsAtSetOf(targetPointer)

                    assertEquals(false, result)
                }
            }
        }
    }

    private val validPointerPairs = mapOf(
        "[]" to emptyList(),
        "[\"name\"]" to listOf(ClaimsPathPointerComponent.String("name")),
        "[null]" to listOf(ClaimsPathPointerComponent.Null),
        "[1]" to listOf(ClaimsPathPointerComponent.Index(1)),
        "[\"name\",null]" to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.Null),
        "[\"name\",1]" to listOf(ClaimsPathPointerComponent.String("name"), ClaimsPathPointerComponent.Index(1)),
        "[\"name\",null,1]" to listOf(
            ClaimsPathPointerComponent.String("name"),
            ClaimsPathPointerComponent.Null,
            ClaimsPathPointerComponent.Index(1)
        ),
        "[null,null]" to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.Null),
        "[0,1]" to listOf(ClaimsPathPointerComponent.Index(0), ClaimsPathPointerComponent.Index(1)),
        "[null,1]" to listOf(ClaimsPathPointerComponent.Null, ClaimsPathPointerComponent.Index(1)),
        "[1,null]" to listOf(ClaimsPathPointerComponent.Index(1), ClaimsPathPointerComponent.Null),
    )
}
