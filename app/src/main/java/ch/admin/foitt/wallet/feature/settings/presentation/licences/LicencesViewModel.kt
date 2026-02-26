package ch.admin.foitt.wallet.feature.settings.presentation.licences

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.licences.model.Library
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LicencesViewModel @Inject constructor(
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.Details(navManager::popBackStack, R.string.tk_settings_licences_title)

    private val _libraries: MutableStateFlow<List<Library>?> = MutableStateFlow(null)
    val libraries = _libraries.asStateFlow()

    private val _licenseDialog: MutableStateFlow<Library?> = MutableStateFlow(null)
    val licenseDialog = _licenseDialog.asStateFlow()

    init {
        val libs = Libs.Builder().withContext(appContext).build()

        _libraries.value = libs.libraries.map { library ->
            val libraryLicense = library.licenses.firstOrNull()
            val license = libs.licenses.firstOrNull { libraryLicense?.name == it.name }
            Library(
                name = library.name,
                version = library.artifactVersion,
                licenseName = license?.name ?: "License name",
                licenseContent = license?.licenseContent ?: license?.url ?: "content"
            )
        }
    }

    fun onLibraryClick(library: Library) {
        _licenseDialog.value = library
    }

    fun onDismissDialog() {
        _licenseDialog.value = null
    }
}
