package projeto.teste.tarefas;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by root on 02/06/18.
 */

@Dao
public interface TarefaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long adicionar(Tarefa m);

    @Query("SELECT * from Tarefa ORDER BY id DESC")
    List<Tarefa> listar();


    @Query("SELECT * from Tarefa WHERE id = :id")
    Tarefa getTarefaPeloId(Integer id);

    @Query("DELETE FROM Tarefa WHERE id = :id")
    void remover(Integer id);
    
}
