package br.com.alura.helloapp.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import br.com.alura.helloapp.DestinosHelloApp
import br.com.alura.helloapp.preferences.PreferencesKey.SENHA
import br.com.alura.helloapp.preferences.PreferencesKey.USUARIO
import br.com.alura.helloapp.preferences.dataStore
import br.com.alura.helloapp.ui.login.FormularioLoginTela
import br.com.alura.helloapp.ui.login.FormularioLoginViewModel
import br.com.alura.helloapp.ui.login.LoginTela
import br.com.alura.helloapp.ui.login.LoginViewModel
import br.com.alura.helloapp.ui.navegaLimpo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun NavGraphBuilder.loginGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = DestinosHelloApp.Login.rota,
        route = DestinosHelloApp.LoginGraph.rota
    ) {
        composable(
            route = DestinosHelloApp.Login.rota,
        ) {
            val viewModel = hiltViewModel<LoginViewModel>()
            val state by viewModel.uiState.collectAsState()

            if (state.logado) {
                LaunchedEffect(Unit) {
                    navController.navegaLimpo(DestinosHelloApp.HomeGraph.rota)
                }
            }

            val dataStore = LocalContext.current.dataStore
            val coroutineScope = rememberCoroutineScope()

            LoginTela(
                state = state,
                onClickLogar = {
                    coroutineScope.launch {
                        dataStore.data.collect { preferences ->
                            val senha = preferences[SENHA]
                            val usuario = preferences[USUARIO]

                            if (usuario == state.usuario && senha == state.senha) {
                                dataStore.edit { loginPreferences ->
                                    loginPreferences[booleanPreferencesKey("logado")] = true
                                }
                                viewModel.tentaLogar()
                            } else {
                                state.onErro(true)
                            }
                        }
                    }
                },
                onClickCriarLogin = {
                    navController.navigate(DestinosHelloApp.FormularioLogin.rota)
                }
            )
        }

        composable(
            route = DestinosHelloApp.FormularioLogin.rota,
        ) {
            val viewModel = hiltViewModel<FormularioLoginViewModel>()
            val state by viewModel.uiState.collectAsState()
            val dataStore = LocalContext.current.dataStore
            val coroutineScope = rememberCoroutineScope()

            FormularioLoginTela(
                state = state,
                onSalvar = {
                    coroutineScope.launch {
                        dataStore.edit { preferences ->
                            preferences[USUARIO] = state.usuario
                            preferences[SENHA] = state.senha
                        }
                    }
                    navController.navegaLimpo(DestinosHelloApp.Login.rota)
                }
            )
        }
    }
}