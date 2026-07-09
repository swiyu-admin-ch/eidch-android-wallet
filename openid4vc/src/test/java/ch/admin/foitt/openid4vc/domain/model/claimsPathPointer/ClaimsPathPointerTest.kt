package ch.admin.foitt.openid4vc.domain.model.claimsPathPointer

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
        val pointerPairs = listOf(
            listOf(nullMock) to listOf(index0Mock),
            listOf(nullMock) to listOf(index1Mock),
            listOf(nullMock, nullMock) to listOf(index0Mock, index0Mock),
            listOf(nullMock, nullMock) to listOf(nullMock, index0Mock),
            listOf(nullMock, nullMock) to listOf(index0Mock, nullMock),
            listOf(nullMock, stringMock) to listOf(index0Mock, stringMock),
            listOf(stringMock, nullMock) to listOf(stringMock, index0Mock),
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
        val pointerPairs = listOf(
            listOf(stringMock) to listOf(ClaimsPathPointerComponent.String("otherName")),
            listOf(stringMock) to listOf(index1Mock),
            listOf(stringMock) to listOf(nullMock),
            listOf(index1Mock) to listOf(index0Mock),
            listOf(index1Mock) to listOf(stringMock),
            listOf(index1Mock) to listOf(nullMock),
            listOf(nullMock) to listOf(stringMock),
            listOf(stringMock, stringMock) to listOf(
                ClaimsPathPointerComponent.String("otherName"),
                ClaimsPathPointerComponent.String("otherName")
            ),
            listOf(stringMock, stringMock) to listOf(stringMock, ClaimsPathPointerComponent.String("otherName")),
            listOf(stringMock, stringMock) to listOf(ClaimsPathPointerComponent.String("otherName"), stringMock),
            listOf(stringMock, stringMock) to listOf(stringMock, index1Mock),
            listOf(stringMock, stringMock) to listOf(index1Mock, stringMock),
            listOf(stringMock, stringMock) to listOf(stringMock, nullMock),
            listOf(stringMock, stringMock) to listOf(nullMock, stringMock),
            listOf(index1Mock, index1Mock) to listOf(index0Mock, index0Mock),
            listOf(index1Mock, index1Mock) to listOf(index1Mock, index0Mock),
            listOf(index1Mock, index1Mock) to listOf(index0Mock, index1Mock),
            listOf(index1Mock, index1Mock) to listOf(nullMock, index1Mock),
            listOf(index1Mock, index1Mock) to listOf(index1Mock, nullMock),
            listOf(index1Mock, index1Mock) to listOf(nullMock, nullMock),
            listOf(index1Mock, stringMock) to listOf(index1Mock, index1Mock),
            listOf(index1Mock, stringMock) to listOf(index1Mock, ClaimsPathPointerComponent.String("otherName")),
            listOf(index1Mock, stringMock) to listOf(index0Mock, stringMock),
            listOf(index1Mock, stringMock) to listOf(index1Mock, nullMock),
            listOf(index1Mock, stringMock) to listOf(nullMock, stringMock),
            listOf(index1Mock, nullMock) to listOf(index1Mock, stringMock),
            listOf(index1Mock, nullMock) to listOf(nullMock, nullMock),
            listOf(stringMock, index1Mock) to listOf(index1Mock, stringMock),
            listOf(stringMock, index1Mock) to listOf(stringMock, index0Mock),
            listOf(stringMock, index1Mock) to listOf(ClaimsPathPointerComponent.String("otherName"), index1Mock),
            listOf(stringMock, index1Mock) to listOf(stringMock, nullMock),
            listOf(nullMock, nullMock) to listOf(stringMock, stringMock),
            listOf(nullMock, nullMock) to listOf(index1Mock, stringMock),
            listOf(nullMock, nullMock) to listOf(stringMock, index1Mock),
            listOf(nullMock, stringMock) to listOf(nullMock, ClaimsPathPointerComponent.String("otherName")),
            listOf(nullMock, stringMock) to listOf(nullMock, index1Mock),
            listOf(nullMock, stringMock) to listOf(index0Mock, ClaimsPathPointerComponent.String("otherName")),
            listOf(nullMock, index1Mock) to listOf(nullMock, nullMock),
            listOf(nullMock, index1Mock) to listOf(nullMock, stringMock),
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

    @TestFactory
    fun `Claim path pointer with no index returns no indices`(): List<DynamicTest> {
        val paths: List<ClaimsPathPointer> = listOf(
            emptyList(),
            listOf(nullMock),
            listOf(stringMock),
            listOf(stringMock, stringMock),
            listOf(nullMock, nullMock),
        )
        return paths.map { path ->
            DynamicTest.dynamicTest("Claims path pointer $path should not return any indices") {
                runTest {
                    val result = path.allIndices

                    assertEquals(0, result.size)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer with indices returns all indices`(): List<DynamicTest> {
        val paths: Map<ClaimsPathPointer, List<Int>> = mapOf(
            listOf(index0Mock) to listOf(0),
            listOf(index0Mock, index0Mock) to listOf(0, 0),
            listOf(index0Mock, index1Mock) to listOf(0, 1),
            listOf(nullMock, index0Mock) to listOf(0),
            listOf(stringMock, index0Mock, nullMock) to listOf(0),
            listOf(index0Mock, nullMock, index1Mock) to listOf(0, 1),
        )
        return paths.map { (path, expectedIndices) ->
            DynamicTest.dynamicTest("Claims path pointer $path should return all indices") {
                runTest {
                    val result = path.allIndices

                    assertEquals(expectedIndices, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer with no wildcard resolves nothing`(): List<DynamicTest> {
        val paths: List<ClaimsPathPointer> = listOf(
            emptyList(),
            listOf(index0Mock),
            listOf(stringMock),
            listOf(index0Mock, index0Mock),
            listOf(index0Mock, stringMock),
        )
        return paths.map { path ->
            DynamicTest.dynamicTest("Claims path pointer $path should not resolve anything") {
                runTest {
                    val result = path.resolveWildCards(listOf(0, 1))

                    assertEquals(path, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer with wildcard but no indices resolves nothing`(): List<DynamicTest> {
        val paths: List<ClaimsPathPointer> = listOf(
            listOf(nullMock),
            listOf(stringMock, nullMock),
        )
        return paths.map { path ->
            DynamicTest.dynamicTest("Claims path pointer $path should not resolve anything") {
                runTest {
                    val result = path.resolveWildCards(emptyList())

                    assertEquals(path, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer with wildcard and indices resolves all wildcards`(): List<DynamicTest> {
        val paths: Map<ClaimsPathPointer, ClaimsPathPointer> = mapOf(
            listOf(nullMock) to listOf(index0Mock),
            listOf(nullMock, nullMock) to listOf(index0Mock, index1Mock),
            listOf(nullMock, nullMock, nullMock) to listOf(index0Mock, index1Mock, ClaimsPathPointerComponent.Index(2)),
            listOf(stringMock, nullMock) to listOf(stringMock, index0Mock),
            listOf(nullMock, stringMock, nullMock) to listOf(index0Mock, stringMock, index1Mock),
        )
        return paths.map { (path, expectedPath) ->
            DynamicTest.dynamicTest("Claims path pointer $path should resolve all wildcards") {
                runTest {
                    val result = path.resolveWildCards(listOf(0, 1, 2))

                    assertEquals(expectedPath, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer with wildcard and not enough indices resolves first wildcards`(): List<DynamicTest> {
        val paths: Map<ClaimsPathPointer, ClaimsPathPointer> = mapOf(
            listOf(nullMock, nullMock) to listOf(index0Mock, nullMock),
            listOf(nullMock, nullMock, nullMock) to listOf(index0Mock, nullMock, nullMock),
            listOf(stringMock, nullMock, nullMock) to listOf(stringMock, index0Mock, nullMock),
            listOf(nullMock, stringMock, nullMock) to listOf(index0Mock, stringMock, nullMock),
        )
        return paths.map { (path, expectedPath) ->
            DynamicTest.dynamicTest("Claims path pointer $path does not only partly resolve wildcards") {
                runTest {
                    val result = path.resolveWildCards(listOf(0))

                    assertEquals(expectedPath, result)
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer where second pointer is smaller returns false`(): List<DynamicTest> {
        val pointerPairs = listOf(
            listOf(stringMock) to emptyList<ClaimsPathPointerComponent>(),
            listOf(index0Mock) to emptyList(),
            listOf(nullMock) to emptyList(),
        )
        return pointerPairs.map { (pointer1, pointer2) ->
            DynamicTest.dynamicTest("Claims path pointer where second pointer is smaller should return false") {
                runTest {
                    assertFalse(pointer1.pointsAtSetOf(pointer2))
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer where first pointer is smaller returns true`(): List<DynamicTest> {
        val pointerPairs = mapOf(
            emptyList<ClaimsPathPointerComponent>() to listOf(stringMock),
            emptyList<ClaimsPathPointerComponent>() to listOf(index0Mock),
            emptyList<ClaimsPathPointerComponent>() to listOf(nullMock),
            listOf(stringMock) to listOf(stringMock, string2Mock),
            listOf(stringMock) to listOf(stringMock, index0Mock),
            listOf(stringMock) to listOf(stringMock, nullMock),
            listOf(index0Mock) to listOf(index0Mock, stringMock),
            listOf(index0Mock) to listOf(index0Mock, index1Mock),
            listOf(index0Mock) to listOf(index0Mock, nullMock),
            listOf(nullMock) to listOf(index0Mock, stringMock),
            listOf(nullMock) to listOf(index0Mock, index1Mock),
            listOf(nullMock) to listOf(index0Mock, nullMock),
            listOf(nullMock) to listOf(nullMock, stringMock),
            listOf(nullMock) to listOf(nullMock, index0Mock),
            listOf(nullMock) to listOf(nullMock, nullMock),
        )
        return pointerPairs.map { (pointer1, pointer2) ->
            DynamicTest.dynamicTest("Paths ${pointer1.toPointerString()} and ${pointer2.toPointerString()} should return true") {
                runTest {
                    assertTrue(pointer1.pointsAtSetOf(pointer2))
                }
            }
        }
    }

    @TestFactory
    fun `Claim path pointer where first pointer is smaller with enforced length returns false`(): List<DynamicTest> {
        val pointerPairs = listOf(
            emptyList<ClaimsPathPointerComponent>() to listOf(stringMock),
            emptyList<ClaimsPathPointerComponent>() to listOf(index0Mock),
            emptyList<ClaimsPathPointerComponent>() to listOf(nullMock),
        )
        return pointerPairs.map { (pointer1, pointer2) ->
            DynamicTest.dynamicTest("Paths ${pointer1.toPointerString()} and ${pointer2.toPointerString()} should return false") {
                runTest {
                    assertFalse(pointer1.pointsAtSetOf(pointer2, enforceLength = true))
                }
            }
        }
    }

    private val nullMock = ClaimsPathPointerComponent.Null
    private val index0Mock = ClaimsPathPointerComponent.Index(0)
    private val index1Mock = ClaimsPathPointerComponent.Index(1)
    private val stringMock = ClaimsPathPointerComponent.String("name")
    private val string2Mock = ClaimsPathPointerComponent.String("name2")

    private val validPointerPairs = mapOf(
        "[]" to emptyList(),
        "[\"name\"]" to listOf(stringMock),
        "[null]" to listOf(nullMock),
        "[1]" to listOf(index1Mock),
        "[\"name\",null]" to listOf(stringMock, nullMock),
        "[\"name\",1]" to listOf(stringMock, index1Mock),
        "[\"name\",null,1]" to listOf(
            stringMock,
            nullMock,
            index1Mock
        ),
        "[null,null]" to listOf(nullMock, nullMock),
        "[0,1]" to listOf(index0Mock, index1Mock),
        "[null,1]" to listOf(nullMock, index1Mock),
        "[1,null]" to listOf(index1Mock, nullMock),
    )
}
