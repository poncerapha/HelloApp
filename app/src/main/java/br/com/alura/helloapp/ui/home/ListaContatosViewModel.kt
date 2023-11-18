package br.com.alura.helloapp.ui.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.helloapp.database.ContatoDao
import br.com.alura.helloapp.preferences.PreferencesKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ListaContatosViewModel @Inject constructor(
    private val contatoDao: ContatoDao,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListaContatosUiState())
    val uiState: StateFlow<ListaContatosUiState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val contatos = contatoDao.buscaTodos()
            contatos.collect {
                _uiState.value = _uiState.value.copy(
                    contatos = it
                )
            }
        }

        viewModelScope.launch {
            carregaNomeUsuario()
        }

        _uiState.update { state ->
            state.copy(
                onMostrarCaixaDialogoDeslogarMudou = {
                    _uiState.value = _uiState.value.copy(
                        mostrarCaixaDialogoDeslogar = it
                    )
                }
            )
        }
    }

    suspend fun buscarParcialmente(valor: String) {
        contatoDao.buscaParcial(valor).collect { contatosBuscados ->
            _uiState.value = _uiState.value.copy(
                contatos = contatosBuscados
            )
        }
    }

    private suspend fun carregaNomeUsuario() {
        val usuario = dataStore.data.first()[PreferencesKey.USUARIO]
        usuario?.let {
            _uiState.value = _uiState.value.copy(
                nomeUsuario = it
            )
        }
    }

   suspend fun desloga() {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("logado")] = false
        }
    }
}