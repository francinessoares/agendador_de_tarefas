package projeto.teste.tarefas;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by root on 02/06/18.
 */

@Entity
public class Tarefa {

    @PrimaryKey(autoGenerate = true)
    private Integer id;
    private Long data;
    private String titulo;
    private String desc;
    private Long horaNotificacao;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getHoraNotificacao() {
        return horaNotificacao;
    }

    public void setHoraNotificacao(Long horaNotificacao) {
        this.horaNotificacao = horaNotificacao;
    }

    public Integer getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setId(Integer id) {
        this.id = id;

    }

    @Override
    public String toString() {
            return titulo+"\n"+new SimpleDateFormat("dd/MM/yy HH:mm").format(data);
    }
}
