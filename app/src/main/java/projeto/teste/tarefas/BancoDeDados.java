package projeto.teste.tarefas;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by root on 02/06/18.
 */

@Database(entities = {Tarefa.class}, version = 2, exportSchema = false)
public abstract class BancoDeDados extends RoomDatabase{

    private static BancoDeDados instancia;

    public static BancoDeDados getInstance(Context context){
        synchronized (Tarefa.class){
            if(instancia == null){
                instancia = Room.databaseBuilder(context.getApplicationContext(),
                        BancoDeDados.class, "banco").build();
            }
        }

        return instancia;
    }

    public abstract TarefaDao getDao();

}
