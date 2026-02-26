package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.TestMrzData
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.getOr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MrzChooserViewModel @Inject constructor(
    safeJson: SafeJson,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(navManager::popBackStack, null)

    private val mockUnderAge = safeJson.safeDecodeStringTo<List<TestMrzData>>(MockMRZData.underAgeMock)
    private val mockAdult = safeJson.safeDecodeStringTo<List<TestMrzData>>(MockMRZData.adultMock)
    private val mockOther = safeJson.safeDecodeStringTo<List<TestMrzData>>(MockMRZData.otherMock)

    val mrzData = mockUnderAge.getOr(emptyList()) + mockAdult.getOr(emptyList()) + mockOther.getOr(emptyList())

    fun onBack() = navManager.popBackStackOrToRoot()

    fun onMrzItemClick(index: Int) {
        viewModelScope.launch {
            navManager.replaceCurrentWith(
                Destination.MrzSubmissionScreen(mrzLines = mrzData[index].mrz)
            )
        }
    }

    private object MockMRZData {
        const val underAgeMock = """
[
  {
    "displayName":"UNDERAGE I7A (ID CARD)",
    "mrz": ["ID<<<I7A<<<<<<7<<<<<<<<<<<<<<<","1001015X3012316<<<<<<<<<<<<<<2","MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE I7G (ID CARD)",
    "mrz": ["ID<<<I7G<<<<<<3<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<0", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE I7C (ID CARD)",
    "mrz": ["ID<<<I7C<<<<<<9<<<<<<<<<<<<<<<", "1001015X2312318<<<<<<<<<<<<<<0", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE I7D (ID CARD)",
    "mrz": ["ID<<<I7D<<<<<<0<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<6", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE I7E (ID CARD)",
    "mrz": ["ID<<<I7E<<<<<<1<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<4", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE I7F (ID CARD)",
    "mrz": ["ID<<<I7F<<<<<<2<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<2", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE I7G (ID CARD)",
    "mrz": ["ID<<<I7G<<<<<<3<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<0", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE P7B (PASSPORT)",
    "mrz": ["PM<<<MINDERJAEHRIGE<<ANNETTE<<<<<<<<<<<<<<<<", "P7B<<<<<<7<<<1001015X3012316<<<<<<<<<<<<<<02"]
  },
  {
    "displayName":"UNDERAGE A (FOREIGN)",
    "mrz": ["AR<<<M101A<<<<4<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<8", "MINDERJAEHRIGER<<TOBI<<<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE B (FOREIGN)",
    "mrz": ["AR<<<M101B<<<<7<<<<<<<<<<<<<<<", "1001015X2312318<<<<<<<<<<<<<<4", "MINDERJAEHRIGER<<TOBI<<<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE C (FOREIGN) DEAD",
    "mrz": ["AR<<<M101C<<<<0<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<6", "MINDERJAEHRIGER<<TOBI<<<<<<<<<"]
  },
  {
    "displayName":"UNDERAGE D (FOREIGN)",
    "mrz": ["AR<<<M101D<<<<3<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<0", "MINDERJAEHRIGER<<TOBI<<<<<<<<<"]
  }
]
"""
        const val adultMock = """
[
  {
    "displayName":"ADULT A (FOREIGN)",
    "mrz": ["AR<<<E101A<<<<8<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<6", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT B (FOREIGN)",
    "mrz": ["AR<<<E101B<<<<1<<<<<<<<<<<<<<<", "0001018X2312318<<<<<<<<<<<<<<2", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT C (FOREIGN) DEAD",
    "mrz": ["AR<<<E101C<<<<4<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<4", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT D (FOREIGN)",
    "mrz": ["AR<<<E101D<<<<7<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<8", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT A (ID CARD)",
    "mrz": ["ID<<<I1A<<<<<<9<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<4", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT C (ID CARD)",
    "mrz": ["ID<<<I1C<<<<<<1<<<<<<<<<<<<<<<", "0001018X2312318<<<<<<<<<<<<<<2", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT D (ID CARD)",
    "mrz": ["ID<<<I1D<<<<<<2<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<8", "MUSTER1<<HANS1<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT B (PASSPORT)",
    "mrz": ["PM<<<MUSTER1<<HANS1<<<<<<<<<<<<<<<<<<<<<<<<<", "P1B<<<<<<9<<<0001018X3012316<<<<<<<<<<<<<<04"]
  },
  {
    "displayName":"ADULT2 (FOREIGN)",
    "mrz": ["AR<<<E102<<<<<5<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<2", "MUSTER2<<HANS2<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT2 (PASSPORT)",
    "mrz": ["PM<<<MUSTER2<<HANS2<<<<<<<<<<<<<<<<<<<<<<<<<", "P2<<<<<<<1<<<0001018X3012316<<<<<<<<<<<<<<00"]
  },
  {
    "displayName":"ADULT3 (FOREIGN)",
    "mrz": ["AR<<<E103<<<<<2<<<<<<<<<<<<<<<", "0001018X2212315<<<<<<<<<<<<<<0", "MUSTER3<<HANS3<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT3 (ID CARD)",
    "mrz": ["ID<<<I3<<<<<<<5<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<2", "MUSTER3<<HANS3<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT4 (FOREIGN)",
    "mrz": ["AR<<<E104<<<<<9<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<4", "MUSTER4<<HANS4<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT4 (ID CARD)",
    "mrz": ["ID<<<I4<<<<<<<8<<<<<<<<<<<<<<<", "0001018X2212315<<<<<<<<<<<<<<8", "MUSTER4<<HANS4<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT5 (FOREIGN) DEAD",
    "mrz": ["AR<<<E105<<<<<6<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<0", "MUSTER5<<HANS5<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT5 (ID CARD)",
    "mrz": ["ID<<<I5<<<<<<<1<<<<<<<<<<<<<<<", "0001018X3012316<<<<<<<<<<<<<<0", "MUSTER5<<HANS5<<<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"ADULT6 (PASSPORT) DEAD",
    "mrz": ["PM<<<MUSTER6<<HANS6<<<<<<<<<<<<<<<<<<<<<<<<<", "P6<<<<<<<3<<<0001018X3012316<<<<<<<<<<<<<<06"]
  }
]
"""
        const val otherMock = """
[
  {
    "displayName":"MUSTERMANN (FOREIGN)",
    "mrz": ["AR<<<E012345676<<<<<<<<<<<<<<<", "7001017X4012313<<<<<<<<<<<<<<6", "MUSTERMANN<<FRANZ<<<<<<<<<<<<<"]
  },
  {
    "displayName":"MUSTERMANN (PASSPORT)",
    "mrz": ["PM<<<MUSTERMANN<<MAX<<<<<<<<<<<<<<<<<<<<<<<<", "X012345679<<<7001017X4012313<<<<<<<<<<<<<<00"]
  },
  {
    "displayName":"BEVORMUNDET (PASSPORT)",
    "mrz": ["PM<<<BEVORMUNDET<<FRITZ<<<<<<<<<<<<<<<<<<<<<", "P9<<<<<<<2<<<0001018X3012316<<<<<<<<<<<<<<08"]
  },
  {
    "displayName":"LORENZ (ID CARD)",
    "mrz": ["ID<<<I7<<<<<<<7<<<<<<<<<<<<<<<", "6704034X3012316<<<<<<<<<<<<<<4", "LORENZ<<PHILIPPE<<<<<<<<<<<<<<"]
  },
  {
    "displayName":"INFOSTAR (PASSPORT)",
    "mrz": ["PM<<<MEIER<<NICHT<IM<INFOSTAR<<<<<<<<<<<<<<<", "P999<<<<<4<<<1001015X3012316<<<<<<<<<<<<<<08"]
  }
]
"""
    }
}
