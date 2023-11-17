package br.com.alura.helloapp.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.helloapp.R
import br.com.alura.helloapp.data.Contato
import br.com.alura.helloapp.database.ContatoDao
import br.com.alura.helloapp.extensions.converteParaDate
import br.com.alura.helloapp.extensions.converteParaString
import br.com.alura.helloapp.util.ID_CONTATO
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FormularioContatoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contatoDao: ContatoDao
) : ViewModel() {

    private val idContato = savedStateHandle.get<Long>(ID_CONTATO)

    private val _uiState = MutableStateFlow(FormularioContatoUiState())
    val uiState: StateFlow<FormularioContatoUiState>
        get() = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            carregaContato()
        }

        _uiState.update { state ->
            state.copy(onNomeMudou = {
                _uiState.value = _uiState.value.copy(
                    nome = it
                )
            }, onSobrenomeMudou = {
                _uiState.value = _uiState.value.copy(
                    sobrenome = it
                )
            }, onTelefoneMudou = {
                _uiState.value = _uiState.value.copy(
                    telefone = it
                )
            }, onFotoPerfilMudou = {
                _uiState.value = _uiState.value.copy(
                    fotoPerfil = it
                )
            }, onAniversarioMudou = {
                _uiState.value = _uiState.value.copy(
                    aniversario = it.converteParaDate(), mostrarCaixaDialogoData = false
                )
            }, onMostrarCaixaDialogoImagem = {
                _uiState.value = _uiState.value.copy(
                    mostrarCaixaDialogoImagem = it
                )
            }, onMostrarCaixaDialogoData = {
                _uiState.value = _uiState.value.copy(
                    mostrarCaixaDialogoData = it
                )
            })
        }
    }

    fun defineTextoAniversario(textoAniversario: String) {
        val textoAniversairo = _uiState.value.aniversario?.converteParaString() ?: textoAniversario

        _uiState.update {
            it.copy(textoAniversairo = textoAniversairo)
        }
    }

    fun carregaImagem(url: String) {
        _uiState.value = _uiState.value.copy(
            fotoPerfil = url, mostrarCaixaDialogoImagem = false
        )
    }

    suspend fun salvarContato() {
        with(_uiState.value) {
            contatoDao.insere(
                Contato(
                    id = id,
                    nome = nome,
                    sobrenome = sobrenome,
                    telefone = telefone,
                    fotoPerfil = fotoPerfil,
                    aniversario = aniversario
                )
            )
        }
    }

    private suspend fun carregaContato() {
        idContato?.let {
            val contato = contatoDao.buscaPorId(it)
            contato.collect {
                it?.let { contato ->
                    with(contato) {
                        _uiState.value = uiState.value.copy(
                            id = id,
                            nome = nome,
                            sobrenome = sobrenome,
                            telefone = telefone,
                            fotoPerfil = fotoPerfil,
                            aniversario = aniversario,
                            tituloAppbar = R.string.titulo_editar_contato
                        )
                    }
                }
            }
        }
    }
}
