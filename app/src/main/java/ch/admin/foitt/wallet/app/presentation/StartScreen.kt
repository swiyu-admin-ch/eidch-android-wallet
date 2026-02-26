package ch.admin.foitt.wallet.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun StartScreen(
    viewModel: StartViewModel,
) {
    LaunchedEffect(key1 = viewModel) {
        viewModel.navigateToFirstScreen()
    }
}
