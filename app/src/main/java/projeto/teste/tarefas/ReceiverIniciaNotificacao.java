package projeto.teste.tarefas;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.AlarmClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by root on 02/06/18.
 */

public class ReceiverIniciaNotificacao extends BroadcastReceiver {

    private static final String ID_CANAL = "canalNotificacaoAgendador";

    @Override
    public void onReceive(Context context, Intent intent) {


        Integer id = new Integer(intent.getIntExtra("id", 0));
        String titulo = intent.getStringExtra("titulo");

        Calendar calendar = Calendar.getInstance();
        Long parada = intent.getLongExtra("parada", calendar.getTimeInMillis());
        Long duracaoNotif = parada - calendar.getTimeInMillis();
        System.out.println("DURACAO DA NOTIFICAÇÃO: " + duracaoNotif);

        Intent intentNotif = new Intent(context, CadastroActivity.class);
        intentNotif.putExtra("id", id);
        PendingIntent intentPendente = PendingIntent.getActivity(context, id, intentNotif, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    ID_CANAL, "Notificações do agendador de tarefas", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(canal);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ID_CANAL)
                .setContentTitle("Uma tarefa está próxima!")
                .setContentText(titulo)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setTimeoutAfter(duracaoNotif)
                .setContentIntent(intentPendente)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .addAction(R.drawable.ic_launcher_background, "Acessar a tarefa", intentPendente)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());

    }

}