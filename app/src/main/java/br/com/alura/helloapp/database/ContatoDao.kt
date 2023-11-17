package br.com.alura.helloapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import br.com.alura.helloapp.data.Contato

@Dao
interface ContatoDao {

    @Insert(onConflict = REPLACE)
    suspend fun insere(contato: Contato)

    @Query("SELECT * FROM Contato")
    suspend fun buscaTodos(): List<Contato>

    @Query("SELECT * FROM Contato WHERE id = :id")
    suspend fun buscaPorId(id: Long): Contato?

    @Query("DELETE FROM Contato WHERE id = :id")
    suspend fun deletaContato(id: Long): Contato?
}