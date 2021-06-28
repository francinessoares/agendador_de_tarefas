package projeto.teste.tarefas;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CadastroActivity extends AppCompatActivity {

    private static final String DURACAO_10_MIN = "10 minutos";
    private static final String DURACAO_30_MIN = "30 minutos";
    private static final String DURACAO_1_HORA = "1 hora";
    private static final String DURACAO_1_DIA = "1 dia";

    private Handler handlerThreadPrincipal;
    private Executor executorThreadDoBanco;

    private Tarefa tarefa;
    private EditText titulo;
    private EditText desc;
    private Button data;
    private Button hora;
    private Spinner tempoAntecedencia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        handlerThreadPrincipal = new Handler(Looper.getMainLooper());
        executorThreadDoBanco = Executors.newSingleThreadExecutor();

        setCamposTela();

        final Calendar calendarioData = Calendar.getInstance();

        final Integer idTarefa = getIntent().getIntExtra("id", 0);
        System.out.println("ID TAREFA: "+idTarefa);

        if (idTarefa > 0) {
            inicializaTarefaEdicao(calendarioData, idTarefa);
        } else {
            atualizaCampoData(calendarioData);
            atualizaCampoHora(calendarioData);
        }

        configuraCamposDataHora(calendarioData);
        populaComboTempoAntec();

        Button botaSalvar = (Button) findViewById(R.id.botaoSalvar);
        botaSalvar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (tarefa == null) {
                                tarefa = new Tarefa();
                            }
                            tarefa.setData(populaData(data, hora).getTime());
                            tarefa.setTitulo(titulo.getText().toString());
                            tarefa.setDesc(desc.getText().toString());
                            tarefa.setHoraNotificacao(populaHoraNotificacao(tarefa.getData()));

                            adicionaTarefa();

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } catch (Exception e) {
                            String msg = "Ocorreu um erro!";
                            if (e instanceof AgendamentoInvalidoException) {
                                msg = e.getMessage();
                            }

                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });

    }

    private void setCamposTela() {
        titulo = findViewById(R.id.tituloInput);
        desc = findViewById(R.id.descInput);
        data = findViewById(R.id.dataInput);
        hora = findViewById(R.id.horaInput);
        tempoAntecedencia = findViewById(R.id.tempoAntec);
    }

    private void inicializaTarefaEdicao(final Calendar calendarioData, final Integer idTarefa) {
        executorThreadDoBanco = Executors.newSingleThreadExecutor();
        rodarNaThreadDoBanco(
                new Runnable() {
                    @Override
                    public void run() {
                        tarefa = BancoDeDados.getInstance(getApplicationContext()).getDao().getTarefaPeloId(idTarefa);
                        rodarNaThreadPrincipal(new Runnable() {
                            @Override
                            public void run() {
                                preencherConteudo(calendarioData);
                            }
                        });
                    }
                });
    }

    private void populaComboTempoAntec() {
        String[] items = new String[]{DURACAO_10_MIN, DURACAO_30_MIN, DURACAO_1_HORA, DURACAO_1_DIA};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        tempoAntecedencia.setAdapter(adapter);
    }

    private void adicionaTarefa() throws AgendamentoInvalidoException {
        if (tarefa.getTitulo() == null || tarefa.getTitulo().isEmpty()) {
            throw new AgendamentoInvalidoException("Título da tarefa deve ser preenchido!");
        } else {
            new AsyncTask<Void, Void, String>() {
                private String retorno = "Tarefa salva com sucesso!";

                @Override
                protected String doInBackground(Void... voids) {
                    Long id = 0l;
                    try {
                        id = BancoDeDados.getInstance(getApplicationContext()).getDao().adicionar(tarefa);
                    } catch (Exception e) {
                        retorno = "Erro ao salvar tarefa!";
                        throw new RuntimeException(e);
                    }

                    try {
                        agendaTarefa(id.intValue());
                    } catch (Exception e) {
                        retorno = "Erro ao agendar tarefa!";
                        throw new RuntimeException(e);
                    }

                    return retorno;
                }

                @Override
                protected void onPostExecute(String retorno) {
                    super.onPostExecute(retorno);
                    Toast.makeText(getApplicationContext(), retorno, Toast.LENGTH_SHORT).show();
                }
            }.execute();
        }
    }

    private void agendaTarefa(Integer id) throws Exception {
        try {
            AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            ReceiverIniciaNotificacao receiver = new ReceiverIniciaNotificacao();
            IntentFilter filter = new IntentFilter("ALARM_ACTION");
            registerReceiver(receiver, filter);

            Intent intent = new Intent("ALARM_ACTION");
            intent.putExtra("titulo", tarefa.getTitulo());
            intent.putExtra("id", id);
            intent.putExtra("parada", tarefa.getData());
            PendingIntent operation = PendingIntent.getBroadcast(this, 0, intent, 0);

            alarms.set(AlarmManager.RTC_WAKEUP, tarefa.getHoraNotificacao(), operation);

        } catch (Exception e) {
            throw e;
        }
    }

    private Long populaHoraNotificacao(Long data) throws AgendamentoInvalidoException {
        Calendar tempoAntes = Calendar.getInstance();
        System.out.println("DATA: " + data);
        tempoAntes.setTimeInMillis(data);

        String opcao = tempoAntecedencia.getSelectedItem().toString();

        switch (opcao) {
            case DURACAO_10_MIN:
                tempoAntes.add(Calendar.MINUTE, -10);
                break;
            case DURACAO_30_MIN:
                tempoAntes.add(Calendar.MINUTE, -30);
                break;
            case DURACAO_1_HORA:
                tempoAntes.add(Calendar.MINUTE, -60);
                break;
            case DURACAO_1_DIA:
                tempoAntes.add(Calendar.DAY_OF_MONTH, -1);
                break;
            default:
                break;
        }

        Long horaNotificacao = tempoAntes.getTimeInMillis();
        System.out.println("HORA NOTIF.: " + horaNotificacao);

        if (horaNotificacao < new Date().getTime()) {
            throw new AgendamentoInvalidoException("Data / hora do agendamento deve ser posterior a atual!");
        }

        return horaNotificacao;
    }

    private Date populaData(Button data, Button hora) throws AgendamentoInvalidoException {
        String dataTexto = data.getText().toString();
        String horaTexto = hora.getText().toString();

        try {
            SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date dataEscolhida = formatoData.parse(dataTexto + " " + horaTexto);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dataEscolhida);

            return calendar.getTime();
        } catch (ParseException e) {
            throw new AgendamentoInvalidoException("Erro ao processar data / hora informada(s)!");
        }

    }

    private void configuraCamposDataHora(final Calendar calendarioData) {

        final DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendarioData.set(Calendar.YEAR, year);
                calendarioData.set(Calendar.MONTH, month);
                calendarioData.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                atualizaCampoData(calendarioData);
            }
        };

        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CadastroActivity.this, datePicker,
                        calendarioData.get(Calendar.YEAR), calendarioData.get(Calendar.MONTH),
                        calendarioData.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final TimePickerDialog.OnTimeSetListener timePicker = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendarioData.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendarioData.set(Calendar.MINUTE, minute);
                atualizaCampoHora(calendarioData);
            }
        };

        hora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(CadastroActivity.this, timePicker,
                        calendarioData.get(Calendar.HOUR_OF_DAY),
                        calendarioData.get(Calendar.MINUTE), true).show();
            }
        });
    }

    private void atualizaCampoData(Calendar calendar) {
        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
        data.setText(formatoData.format(calendar.getTime()));
    }

    private void atualizaCampoHora(Calendar calendar) {
        SimpleDateFormat formatoData = new SimpleDateFormat("HH:mm");
        hora.setText(formatoData.format(calendar.getTime()));
    }

    public void openMainActivity() {
        Intent MainActivity = new Intent(this, MainActivity.class);
        startActivity(MainActivity);
    }

    public void preencherConteudo(Calendar calendar) {
        titulo.setText(tarefa.getTitulo());
        desc.setText(tarefa.getDesc());

        calendar.setTimeInMillis(tarefa.getData());

        atualizaCampoData(calendar);
        atualizaCampoHora(calendar);
    }

    void rodarNaThreadPrincipal(Runnable acao) {
        handlerThreadPrincipal.post(acao);
    }

    void rodarNaThreadDoBanco(Runnable acao) {
        executorThreadDoBanco.execute(acao);
    }


}