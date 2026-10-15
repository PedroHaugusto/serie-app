package com.serie.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.navigation.SerieNavHost
import com.serie.app.ui.theme.NocturneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { SerieApp() }
    }
}

@Composable
private fun SerieApp(viewModel: SerieViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // O tema nao segue o sistema: e uma preferencia do usuario, editavel na
    // tela de perfil, como o prototipo define.
    NocturneTheme(dark = state.preferences.darkTheme) {
        SerieNavHost(viewModel = viewModel)
    }
}
