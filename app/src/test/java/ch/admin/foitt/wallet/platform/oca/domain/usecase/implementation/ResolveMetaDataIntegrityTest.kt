package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDataIntegrity
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.ResolveMetaDataIntegrity
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class ResolveMetaDataIntegrityTest {
    private lateinit var resolveMetaDataIntegrity: ResolveMetaDataIntegrity
    private val defaultIntegrityData = MetaDataIntegrity(
        vctMetadataUri = null,
        vctMetadataUriIntegrity = null,
        vct = "https://vct_example",
        vctIntegrity = null
    )

    @BeforeEach
    fun setup() {
        resolveMetaDataIntegrity = ResolveMetaDataIntegrityImpl()
    }

    @Test
    fun `Valid vct_metadata_uri resolves to correct vct_metadata_uri#integrity Pair`(): Unit = runTest {
        val integrityData = defaultIntegrityData.copy(vctMetadataUri = "https://metadataUri")
        assertPairOK(integrityData, Pair(URL("https://metadataUri"), null))
    }

    @Test
    fun `Valid vct_metadata_uri and vct_metadata_uri#integrity resolves to correct Pair`(): Unit = runTest {
        val integrityData = defaultIntegrityData.copy(vctMetadataUri = "https://metadataUri", vctMetadataUriIntegrity = "Some integrity")
        assertPairOK(integrityData, Pair(URL("https://metadataUri"), "Some integrity"))
    }

    @Test
    fun `Null vct_metadata_uri results in correct pair with vct URL`(): Unit = runTest {
        assertPairOK(defaultIntegrityData, Pair(URL("https://vct_example"), null))
    }

    @Test
    fun `Null vct_metadata_uri results in correct pair with vct URL that ignores vctMetadataUriIntegrity`(): Unit = runTest {
        val integrityData = defaultIntegrityData.copy(vctMetadataUriIntegrity = "THIS SHOULD BE IGNORED")
        assertPairOK(integrityData, Pair(URL("https://vct_example"), null))
    }

    @Test
    fun `VCT and vct_integrity are correctly paired to each other`(): Unit = runTest {
        val integrityData = defaultIntegrityData.copy(vctIntegrity = "vctIntegrity")
        assertPairOK(integrityData, Pair(URL("https://vct_example"), "vctIntegrity"))
    }

    @Test
    fun `Non url vct is OK if no vct integrity is set`(): Unit = runTest {
        val integrityData = defaultIntegrityData.copy(vct = "noUrl")
        assertPairOK(integrityData, Pair(null, null))
    }

    @Test
    fun `Invalid vct_metadata_uri resolves to error`(): Unit = runTest {
        val integrityData = defaultIntegrityData.copy(vctMetadataUri = "example")
        resolveMetaDataIntegrity(integrityData).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Unexpected Error thrown if vct uri is invalid while a vct_integrity is set`(): Unit = runTest {
        val integrityData = defaultIntegrityData.copy(vct = "invalid", vctIntegrity = "Some integrity")
        resolveMetaDataIntegrity(integrityData).assertErrorType(OcaError.Unexpected::class)
    }

    @OptIn(UnsafeResultValueAccess::class)
    private suspend fun assertPairOK(integrityData: MetaDataIntegrity, expectedPair: Pair<URL?, String?>) {
        val result = resolveMetaDataIntegrity(integrityData).assertOk()
        assertEquals(result, expectedPair)
    }
}
