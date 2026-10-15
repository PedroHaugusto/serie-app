package com.serie.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.serie.app.ui.SerieViewModel
import com.serie.app.ui.components.Ph
import com.serie.app.ui.components.SerieTab
import com.serie.app.ui.components.SerieTabBar
import com.serie.app.ui.screens.BibliotecaScreen
import com.serie.app.ui.screens.CorpoScreen
import com.serie.app.ui.screens.EditarTreinoScreen
import com.serie.app.ui.screens.EvolucaoScreen
import com.serie.app.ui.screens.ExecucaoScreen
import com.serie.app.ui.screens.HistoricoScreen
import com.serie.app.ui.screens.HojeScreen
import com.serie.app.ui.screens.OnboardingScreen
import com.serie.app.ui.screens.PerfilScreen
import com.serie.app.ui.screens.ResumoScreen
import com.serie.app.ui.screens.TreinosScreen
import com.serie.app.ui.theme.Nocturne

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOJE = "hoje"
    const val TREINOS = "treinos"
    const val EVOLUCAO = "evolucao"
    const val PERFIL = "perfil"
    const val EDITAR = "editar"
    const val BIBLIOTECA = "biblioteca"
    const val EXECUCAO = "execucao"
    const val RESUMO = "resumo"
    const val CORPO = "corpo"
    const val HISTORICO = "historico"
}

/** As quatro rotas que aparecem na barra de abas. */
private val TAB_ROUTES = listOf(Routes.HOJE, Routes.TREINOS, Routes.EVOLUCAO, Routes.PERFIL)

private val TABS = listOf(
    SerieTab(Routes.HOJE, "Hoje", Ph.house),
    SerieTab(Routes.TREINOS, "Treinos", Ph.barbell),
    SerieTab(Routes.EVOLUCAO, "Evolução", Ph.chartLineUp),
    SerieTab(Routes.PERFIL, "Perfil", Ph.user),
)

@Composable
fun SerieNavHost(
    viewModel: SerieViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showTabs = route in TAB_ROUTES

    Column(
        Modifier
            .fillMaxSize()
            .background(Nocturne.colors.bg)
    ) {
        Box(
            Modifier
                .weight(1f)
                .statusBarsPadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.ONBOARDING,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onEnter = {
                            navController.navigate(Routes.HOJE) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        },
                    )
                }

                composable(Routes.HOJE) {
                    HojeScreen(
                        state = state,
                        viewModel = viewModel,
                        onStartWorkout = { id ->
                            viewModel.startWorkout(id)
                            navController.navigate(Routes.EXECUCAO)
                        },
                        onOpenRecords = { navController.navigate(Routes.EVOLUCAO) },
                    )
                }

                composable(Routes.TREINOS) {
                    TreinosScreen(
                        state = state,
                        viewModel = viewModel,
                        onEditWorkout = { id ->
                            viewModel.startEditing(id)
                            navController.navigate(Routes.EDITAR)
                        },
                        onNewWorkout = {
                            viewModel.startEditing(null)
                            navController.navigate(Routes.EDITAR)
                        },
                    )
                }

                composable(Routes.EVOLUCAO) {
                    EvolucaoScreen(
                        state = state,
                        viewModel = viewModel,
                        onOpenBody = { navController.navigate(Routes.CORPO) },
                        onStartWorkout = {
                            navController.navigate(Routes.HOJE) {
                                popUpTo(Routes.HOJE) { inclusive = true }
                            }
                        },
                        onOpenHistory = { navController.navigate(Routes.HISTORICO) },
                    )
                }

                composable(Routes.PERFIL) {
                    PerfilScreen(state = state, viewModel = viewModel)
                }

                composable(Routes.EDITAR) {
                    EditarTreinoScreen(
                        state = state,
                        viewModel = viewModel,
                        onClose = {
                            viewModel.discardDraft()
                            navController.popBackStack()
                        },
                        onSave = {
                            viewModel.saveDraft()
                            navController.popBackStack()
                        },
                        onDelete = {
                            viewModel.deleteDraft()
                            navController.popBackStack()
                        },
                        onAddExercise = { navController.navigate(Routes.BIBLIOTECA) },
                    )
                }

                composable(Routes.BIBLIOTECA) {
                    BibliotecaScreen(
                        state = state,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onConfirm = {
                            viewModel.confirmLibrarySelection()
                            navController.popBackStack()
                        },
                    )
                }

                composable(Routes.EXECUCAO) {
                    ExecucaoScreen(
                        state = state,
                        viewModel = viewModel,
                        onDiscarded = {
                            navController.navigate(Routes.HOJE) {
                                popUpTo(Routes.HOJE) { inclusive = true }
                            }
                        },
                        onFinish = {
                            viewModel.finishWorkout()
                            navController.navigate(Routes.RESUMO) {
                                popUpTo(Routes.HOJE)
                            }
                        },
                    )
                }

                composable(Routes.RESUMO) {
                    ResumoScreen(
                        state = state,
                        viewModel = viewModel,
                        onClose = {
                            viewModel.clearSummary()
                            navController.navigate(Routes.HOJE) {
                                popUpTo(Routes.HOJE) { inclusive = true }
                            }
                        },
                    )
                }

                composable(Routes.HISTORICO) {
                    HistoricoScreen(
                        state = state,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.CORPO) {
                    CorpoScreen(
                        state = state,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }

        if (showTabs) {
            SerieTabBar(
                tabs = TABS,
                selectedId = route.orEmpty(),
                onSelect = { id ->
                    // Trocar de aba nao empilha: volta ate a raiz e restaura o
                    // estado da aba, para o botao voltar nao percorrer o
                    // historico de abas visitadas.
                    navController.navigate(id) {
                        popUpTo(Routes.HOJE) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.navigationBarsPadding(),
            )
        }
    }
}
